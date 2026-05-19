#!/usr/bin/env python3
"""
Seed PetFace demo data for the closed-loop lost-pet workflow.

It creates demo users, pet profiles, PetFace embeddings, and query images:

PetFace individual images A/B/C -> registered pet album + averaged database embedding
PetFace individual image D       -> demo query image for lost-pet search

The source PetFace dataset is read-only. Only backEnd/uploads and MySQL are changed.
"""

from __future__ import annotations

import argparse
import csv
import json
import random
import shutil
import sys
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Any

import pymysql
import requests


DEFAULT_PETFACE_ROOT = Path("/Volumes/ORGOS - Data/PetFaceWorkspace/ai_petface/data/PetFace")
DEFAULT_UPLOAD_DIR = Path(__file__).resolve().parents[1] / "uploads" / "images"
DEFAULT_QUERY_DIR = Path(__file__).resolve().parents[1] / "uploads" / "demo_queries"
DEFAULT_RECOMMENDED_DIR = Path(__file__).resolve().parents[1] / "uploads" / "demo_test_images" / "recommended"
DEFAULT_REPORT = Path(__file__).resolve().parents[1] / "uploads" / "demo_queries" / "seed_report.json"

IMAGE_EXTS = {".jpg", ".jpeg", ".png", ".webp"}

DOG_NAMES = [
    "可乐", "拿铁", "旺财", "皮蛋", "布丁", "小七", "团子", "雪球",
    "摩卡", "奶盖", "阿福", "饼干", "Lucky", "Max", "Coco",
]
CAT_NAMES = [
    "花卷", "年糕", "橘子", "小灰", "奶酪", "丸子", "布偶", "芝麻",
    "糯米", "小狸", "Milo", "Luna", "Neko", "Mimi", "Kitty",
]
OTHER_NAMES = [
    "泡芙", "桃桃", "瓜瓜", "豆包", "米粒", "小乖", "跳跳", "星星",
]
OWNER_NAMES = [
    "陈思远", "林雨桐", "周嘉宁", "王若溪", "李明轩", "赵一诺", "黄子涵", "吴佳怡",
    "刘晨", "张婉清", "许安然", "孙浩宇", "郭芷晴", "何嘉乐", "马亦辰", "朱可欣",
    "梁知夏", "罗景行", "郑语嫣", "唐沐阳", "沈星河", "韩若琳", "曹宇航", "谢念初",
    "叶清越", "冯嘉树", "程予安", "邓诗涵", "蒋牧之", "蔡云舒",
]
PHONE_PREFIXES = [
    "1382168", "1395842", "1503679", "1527481", "1579026",
    "1584317", "1662753", "1736904", "1785629", "1869051",
]


@dataclass
class DemoSample:
    animal: str
    identity: str
    breed: str
    gender: str
    profile_images: list[Path]
    query_candidates: list[Path]


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Seed PetFace demo pet profiles and embeddings.")
    parser.add_argument("--petface-root", default=str(DEFAULT_PETFACE_ROOT))
    parser.add_argument("--animals", nargs="+", default=["dog", "cat"])
    parser.add_argument("--limit", type=int, default=60, help="Total demo pets to create.")
    parser.add_argument("--profile-images-per-pet", type=int, default=3, help="Images used to build one averaged pet feature.")
    parser.add_argument("--query-candidates-per-pet", type=int, default=6, help="Candidate later-query images to score; the best one is copied for demo testing.")
    parser.add_argument("--min-query-score", type=float, default=0.60, help="Skip demo identities whose best query image is below this cosine score.")
    parser.add_argument("--seed", type=int, default=20260519)
    parser.add_argument("--ai-url", default="http://localhost:8000")
    parser.add_argument("--upload-dir", default=str(DEFAULT_UPLOAD_DIR))
    parser.add_argument("--query-dir", default=str(DEFAULT_QUERY_DIR))
    parser.add_argument("--recommended-dir", default=str(DEFAULT_RECOMMENDED_DIR))
    parser.add_argument("--image-url-prefix", default="http://localhost:8080/upload/images")
    parser.add_argument("--report", default=str(DEFAULT_REPORT))
    parser.add_argument("--mysql-host", default="127.0.0.1")
    parser.add_argument("--mysql-port", type=int, default=3306)
    parser.add_argument("--mysql-user", default="root")
    parser.add_argument("--mysql-password", default="123456")
    parser.add_argument("--mysql-db", default="petSql")
    parser.add_argument("--dry-run", action="store_true", help="Only scan and print planned samples.")
    parser.add_argument("--clear-demo", action="store_true", help="Delete previous DEMO-PETFACE pets/users first.")
    return parser.parse_args()


def load_annotations(petface_root: Path, animal: str) -> dict[str, dict[str, str]]:
    path = petface_root / "annotations" / f"{animal}.csv"
    if not path.exists():
        return {}
    with path.open(newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        return {row.get("Name", ""): row for row in reader if row.get("Name")}


def normalize_gender(raw: str | None) -> str:
    value = (raw or "").strip().lower()
    if value in {"male", "m", "公"}:
        return "公"
    if value in {"female", "f", "母"}:
        return "母"
    return "不详"


def normalize_breed(raw: str | None, animal: str) -> str:
    value = (raw or "").strip()
    if not value or value.lower() == "unknown":
        if animal == "dog":
            return "未知犬种"
        if animal == "cat":
            return "未知猫种"
        return "未知品种"
    return value


def demo_pet_name(animal: str, identity: str, index: int) -> str:
    if animal == "dog":
        base = DOG_NAMES[(index - 1) % len(DOG_NAMES)]
    elif animal == "cat":
        base = CAT_NAMES[(index - 1) % len(CAT_NAMES)]
    else:
        base = OTHER_NAMES[(index - 1) % len(OTHER_NAMES)]
    return f"{base}-{identity}"


def image_files(identity_dir: Path) -> list[Path]:
    return sorted(p for p in identity_dir.iterdir() if p.is_file() and p.suffix.lower() in IMAGE_EXTS)


def collect_samples(
    petface_root: Path,
    animals: list[str],
    limit: int,
    seed: int,
    profile_images_per_pet: int,
    query_candidates_per_pet: int,
) -> list[DemoSample]:
    rng = random.Random(seed)
    per_animal = max(1, limit // max(1, len(animals)))
    extra = max(0, limit - per_animal * len(animals))
    samples: list[DemoSample] = []

    for animal_index, animal in enumerate(animals):
        animal_limit = per_animal + (1 if animal_index < extra else 0)
        animal_dir = petface_root / "images" / animal
        if not animal_dir.exists():
            print(f"[warn] skip missing animal dir: {animal_dir}", file=sys.stderr)
            continue
        annotations = load_annotations(petface_root, animal)
        identity_dirs = [p for p in sorted(animal_dir.iterdir()) if p.is_dir()]
        rng.shuffle(identity_dirs)

        selected = 0
        for identity_dir in identity_dirs:
            files = image_files(identity_dir)
            if len(files) < profile_images_per_pet + 1:
                continue
            rng.shuffle(files)
            query_candidates = files[profile_images_per_pet:profile_images_per_pet + query_candidates_per_pet]
            if not query_candidates:
                continue
            identity = identity_dir.name
            ann = annotations.get(identity, {})
            samples.append(DemoSample(
                animal=animal,
                identity=identity,
                breed=normalize_breed(ann.get("Breed"), animal),
                gender=normalize_gender(ann.get("Gender")),
                profile_images=files[:profile_images_per_pet],
                query_candidates=query_candidates,
            ))
            selected += 1
            if selected >= animal_limit:
                break

    return samples[:limit]


def connect_mysql(args: argparse.Namespace):
    return pymysql.connect(
        host=args.mysql_host,
        port=args.mysql_port,
        user=args.mysql_user,
        password=args.mysql_password,
        database=args.mysql_db,
        charset="utf8mb4",
        autocommit=False,
        cursorclass=pymysql.cursors.DictCursor,
    )


def ensure_ai_ready(ai_url: str) -> None:
    resp = requests.get(f"{ai_url.rstrip('/')}/health", timeout=30)
    resp.raise_for_status()
    data = resp.json()
    petface = data.get("petface", {})
    if not petface.get("loaded"):
        raise RuntimeError(f"PetFace model is not loaded: {data}")


def embed_image(ai_url: str, image_path: Path) -> dict[str, Any]:
    with image_path.open("rb") as f:
        resp = requests.post(
            f"{ai_url.rstrip('/')}/api/petface/embed",
            files={"file": (image_path.name, f, "image/png")},
            timeout=180,
        )
    resp.raise_for_status()
    payload = resp.json()
    if payload.get("code") != 200:
        raise RuntimeError(payload)
    return payload["data"]


def copy_demo_image(src: Path, dest_dir: Path, filename: str) -> Path:
    dest_dir.mkdir(parents=True, exist_ok=True)
    suffix = src.suffix.lower() if src.suffix else ".png"
    dest = dest_dir / f"{filename}{suffix}"
    shutil.copy2(src, dest)
    return dest


def clear_demo_data(conn) -> None:
    with conn.cursor() as cur:
        cur.execute("SELECT DISTINCT owner_id FROM Pet WHERE chip_number LIKE 'DEMO-PETFACE-%'")
        owner_ids = [int(row["owner_id"]) for row in cur.fetchall()]
        cur.execute("DELETE FROM Pet WHERE chip_number LIKE 'DEMO-PETFACE-%'")
        if owner_ids:
            placeholders = ",".join(["%s"] * len(owner_ids))
            cur.execute(f"DELETE FROM User WHERE user_id IN ({placeholders})", owner_ids)
        cur.execute("DELETE FROM User WHERE username LIKE '演示用户_%'")
        cur.execute("DELETE FROM User WHERE avatar LIKE 'https://api.dicebear.com/7.x/avataaars/svg?seed=petface-owner-%'")
    conn.commit()


def clear_demo_files(upload_dir: Path, query_dir: Path, recommended_dir: Path) -> None:
    for directory, patterns in [
        (upload_dir, ["demo_petface_*_profile_*.*", "demo_petface_*_profile.*"]),
        (query_dir, ["demo_petface_*_query.*"]),
        (recommended_dir, ["demo_petface_*_query.*", "seed_report.json"]),
    ]:
        directory.mkdir(parents=True, exist_ok=True)
        for pattern in patterns:
            for path in directory.glob(pattern):
                if path.is_file():
                    path.unlink()


def upsert_user(cur, index: int, animal: str, identity: str) -> int:
    prefix = PHONE_PREFIXES[(index - 1) % len(PHONE_PREFIXES)]
    phone = f"{prefix}{index:04d}"
    username = OWNER_NAMES[(index - 1) % len(OWNER_NAMES)]
    if index > len(OWNER_NAMES):
        username = f"{username}{index // len(OWNER_NAMES) + 1}"
    avatar = f"https://api.dicebear.com/7.x/avataaars/svg?seed=petface-owner-{index}"
    cur.execute(
        """
        INSERT INTO User(username, phone, password, avatar, created_at)
        VALUES(%s, %s, %s, %s, NOW())
        ON DUPLICATE KEY UPDATE username = VALUES(username), avatar = VALUES(avatar)
        """,
        (username, phone, "$2a$10$demo.petface.password.hash", avatar),
    )
    cur.execute("SELECT user_id FROM User WHERE phone = %s", (phone,))
    return int(cur.fetchone()["user_id"])


def upsert_pet(cur, owner_id: int, sample: DemoSample, avatar_url: str, index: int) -> int:
    pet_type = 0 if sample.animal == "dog" else 1 if sample.animal == "cat" else 2
    chip_number = f"DEMO-PETFACE-{sample.animal.upper()}-{sample.identity}"
    pet_name = demo_pet_name(sample.animal, sample.identity, index)
    cur.execute("SELECT id FROM Pet WHERE chip_number = %s", (chip_number,))
    existing = cur.fetchone()
    if existing:
        pet_id = int(existing["id"])
        cur.execute(
            """
            UPDATE Pet
            SET owner_id=%s, pet_name=%s, pet_type=%s, breed=%s, gender=%s,
                birth_date='2021-05-19', weight=%s, color=%s,
                distinctive_features=%s, avatar=%s
            WHERE id=%s
            """,
            (
                owner_id,
                pet_name,
                pet_type,
                sample.breed,
                sample.gender,
                8.0 if sample.animal == "dog" else 4.5,
                "PetFace演示样本",
                f"PetFace ID {sample.identity}，用于同宠检索闭环演示",
                avatar_url,
                pet_id,
            ),
        )
        return pet_id

    cur.execute(
        """
        INSERT INTO Pet(
            owner_id, pet_name, pet_type, breed, gender, birth_date, weight,
            color, distinctive_features, chip_number, avatar
        )
        VALUES(%s, %s, %s, %s, %s, '2021-05-19', %s, %s, %s, %s, %s)
        """,
        (
            owner_id,
            pet_name,
            pet_type,
            sample.breed,
            sample.gender,
            8.0 if sample.animal == "dog" else 4.5,
            "PetFace演示样本",
            f"PetFace ID {sample.identity}，用于同宠检索闭环演示",
            chip_number,
            avatar_url,
        ),
    )
    return int(cur.lastrowid)


def upsert_pet_images(cur, pet_id: int, image_urls: list[str]) -> None:
    cur.execute("DELETE FROM Image WHERE pet_id = %s AND postadoption_id IS NULL AND postlost_id IS NULL", (pet_id,))
    for index, image_url in enumerate(image_urls):
        cur.execute(
            "INSERT INTO Image(image_url, sort_order, pet_id) VALUES(%s, %s, %s)",
            (image_url, index, pet_id),
        )


def average_embeddings(embedding_payloads: list[dict[str, Any]]) -> dict[str, Any]:
    vectors = [payload["embedding"] for payload in embedding_payloads if payload.get("embedding")]
    if not vectors:
        raise RuntimeError("No embedding vectors to average")
    dim = len(vectors[0])
    values = [0.0] * dim
    count = 0
    for vector in vectors:
        if len(vector) != dim:
            continue
        count += 1
        for i, value in enumerate(vector):
            values[i] += float(value)
    values = [value / max(count, 1) for value in values]
    norm = sum(value * value for value in values) ** 0.5
    if norm > 0:
        values = [value / norm for value in values]
    first = embedding_payloads[0]
    return {
        "embedding": values,
        "embedding_dim": dim,
        "model_version": first.get("model_version", "PetFace-ID-2.0"),
    }


def cosine(a: list[float], b: list[float]) -> float:
    dot = sum(x * y for x, y in zip(a, b))
    norm_a = sum(x * x for x in a) ** 0.5
    norm_b = sum(y * y for y in b) ** 0.5
    return dot / max(norm_a * norm_b, 1e-12)


def upsert_embedding(cur, pet_id: int, image_urls: list[str], embedding_data: dict[str, Any]) -> None:
    embedding = embedding_data["embedding"]
    avatar_url = image_urls[0]
    cur.execute(
        """
        INSERT INTO pet_face_embedding(
            pet_id, image_url, image_urls, image_count, model_version,
            embedding_dim, embedding_json, embedding_items_json
        )
        VALUES(%s, %s, %s, %s, %s, %s, %s, %s)
        ON DUPLICATE KEY UPDATE
            image_url=VALUES(image_url),
            image_urls=VALUES(image_urls),
            image_count=VALUES(image_count),
            model_version=VALUES(model_version),
            embedding_dim=VALUES(embedding_dim),
            embedding_json=VALUES(embedding_json),
            embedding_items_json=VALUES(embedding_items_json),
            updated_at=CURRENT_TIMESTAMP
        """,
        (
            pet_id,
            avatar_url,
            json.dumps(image_urls, ensure_ascii=False),
            len(image_urls),
            embedding_data.get("model_version", "PetFace-ID-2.0"),
            int(embedding_data.get("embedding_dim", len(embedding))),
            json.dumps(embedding, ensure_ascii=False),
            json.dumps(embedding_data.get("items", []), ensure_ascii=False),
        ),
    )


def seed(args: argparse.Namespace) -> None:
    petface_root = Path(args.petface_root).expanduser()
    upload_dir = Path(args.upload_dir).expanduser()
    query_dir = Path(args.query_dir).expanduser()
    recommended_dir = Path(args.recommended_dir).expanduser()
    report_path = Path(args.report).expanduser()

    samples = collect_samples(
        petface_root,
        args.animals,
        max(args.limit * 4, args.limit),
        args.seed,
        args.profile_images_per_pet,
        args.query_candidates_per_pet,
    )
    if not samples:
        raise RuntimeError("No valid PetFace identities found. Check --petface-root and --animals.")

    random.Random(args.seed).shuffle(samples)
    print(f"planned candidate pets: {len(samples)}; target demo pets: {args.limit}")
    for sample in samples[:8]:
        profile_names = ",".join(path.name for path in sample.profile_images)
        query_names = ",".join(path.name for path in sample.query_candidates)
        print(f"  {sample.animal}/{sample.identity} breed={sample.breed} profiles={profile_names} query_candidates={query_names}")
    if args.dry_run:
        return

    ensure_ai_ready(args.ai_url)
    conn = connect_mysql(args)
    report: list[dict[str, Any]] = []
    try:
        if args.clear_demo:
            clear_demo_data(conn)
            clear_demo_files(upload_dir, query_dir, recommended_dir)

        with conn.cursor() as cur:
            for sample in samples:
                if len(report) >= args.limit:
                    break
                index = len(report) + 1
                stable = f"demo_petface_{sample.animal}_{sample.identity}"
                profile_copies = [
                    copy_demo_image(src, upload_dir, f"{stable}_profile_{image_index}")
                    for image_index, src in enumerate(sample.profile_images, start=1)
                ]
                image_urls = [
                    f"{args.image_url_prefix.rstrip('/')}/{profile_copy.name}"
                    for profile_copy in profile_copies
                ]
                avatar_url = image_urls[0]

                print(f"[{index}/{args.limit}] embedding {sample.animal}/{sample.identity} with {len(profile_copies)} profile images ...")
                profile_payloads = [
                    embed_image(args.ai_url, profile_copy)
                    for profile_copy in profile_copies
                ]
                embedding_data = average_embeddings(profile_payloads)
                embedding_data["items"] = [
                    {"imageUrl": image_url, "embedding": payload["embedding"]}
                    for image_url, payload in zip(image_urls, profile_payloads)
                ]
                query_payloads = [(src, embed_image(args.ai_url, src)) for src in sample.query_candidates]
                query_source, query_payload = max(
                    query_payloads,
                    key=lambda item: max(
                        cosine(profile_payload["embedding"], item[1]["embedding"])
                        for profile_payload in profile_payloads
                    ),
                )
                query_score = max(
                    cosine(profile_payload["embedding"], query_payload["embedding"])
                    for profile_payload in profile_payloads
                )
                if query_score < args.min_query_score:
                    print(
                        f"  skip {sample.animal}/{sample.identity}: "
                        f"best query score {query_score:.4f} < {args.min_query_score:.2f}"
                    )
                    for profile_copy in profile_copies:
                        if profile_copy.exists():
                            profile_copy.unlink()
                    continue
                query_copy = copy_demo_image(query_source, query_dir, f"{stable}_query")
                recommended_copy = copy_demo_image(query_source, recommended_dir, f"{stable}_query")

                user_id = upsert_user(cur, index, sample.animal, sample.identity)
                pet_id = upsert_pet(cur, user_id, sample, avatar_url, index)
                upsert_pet_images(cur, pet_id, image_urls)
                upsert_embedding(cur, pet_id, image_urls, embedding_data)
                conn.commit()

                report.append({
                    "pet_id": pet_id,
                    "animal": sample.animal,
                    "identity": sample.identity,
                    "breed": sample.breed,
                    "gender": sample.gender,
                    "avatar_url": avatar_url,
                    "profile_images": [str(path) for path in profile_copies],
                    "profile_image_count": len(profile_copies),
                    "query_image": str(query_copy),
                    "recommended_query_image": str(recommended_copy),
                    "query_similarity_to_profile": round(query_score, 4),
                    "owner_name": OWNER_NAMES[(index - 1) % len(OWNER_NAMES)],
                    "owner_phone": f"{PHONE_PREFIXES[(index - 1) % len(PHONE_PREFIXES)]}{index:04d}",
                    "expected_pet_name": demo_pet_name(sample.animal, sample.identity, index),
                })
                time.sleep(0.02)
    finally:
        conn.close()

    report_path.parent.mkdir(parents=True, exist_ok=True)
    payload = {
        "created_at": time.strftime("%Y-%m-%d %H:%M:%S"),
        "petface_root": str(petface_root),
        "recommended_dir": str(recommended_dir),
        "count": len(report),
        "samples": report,
    }
    report_path.write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")
    (recommended_dir / "seed_report.json").write_text(json.dumps(payload, ensure_ascii=False, indent=2), encoding="utf-8")
    print(f"saved report: {report_path}")
    print(f"query images: {query_dir}")


if __name__ == "__main__":
    seed(parse_args())
