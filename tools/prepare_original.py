#!/usr/bin/env python3
"""Recover original Swashbucklers client assets/model classes for the Fabric port.

The original 2.6.6B JAR is resolved by Gradle from CurseMaven. We deliberately
copy only assets/hpm and the six Blockbench-generated model classes; no NeoForge
loader metadata or gameplay classes are carried into the Fabric artifact.

Minecraft 26.1 renamed net.minecraft.resources.ResourceLocation to Identifier.
The generated model bytecode otherwise remains usable, so this script rewrites
only CONSTANT_Utf8 entries containing that internal class name. The resulting
classes are then included in the normal Loom input JAR and remapped by Loom.
"""

from __future__ import annotations

import shutil
import struct
import sys
import zipfile
from pathlib import Path

MODEL_CLASSES = (
    "hal/studios/hpm/client/model/Modelraft.class",
    "hal/studios/hpm/client/model/Modelswashbuckler.class",
    "hal/studios/hpm/client/model/Modelswashbucklerupgraded.class",
    "hal/studios/hpm/client/model/Modelcutterremastered.class",
    "hal/studios/hpm/client/model/Modelcutterweaponisedremastered.class",
    "hal/studios/hpm/client/model/Modelcorvetteclass.class",
)

OLD_NAME = "net/minecraft/resources/ResourceLocation"
NEW_NAME = "net/minecraft/resources/Identifier"


def patch_constant_pool_utf8(data: bytes) -> bytes:
    if data[:4] != b"\xca\xfe\xba\xbe":
        raise ValueError("not a Java class file")

    cp_count = struct.unpack_from(">H", data, 8)[0]
    pos = 10
    out = bytearray(data[:10])
    index = 1

    while index < cp_count:
        tag = data[pos]
        out.append(tag)
        pos += 1

        if tag == 1:  # CONSTANT_Utf8
            length = struct.unpack_from(">H", data, pos)[0]
            pos += 2
            raw = data[pos:pos + length]
            pos += length
            text = raw.decode("utf-8")
            patched = text.replace(OLD_NAME, NEW_NAME).encode("utf-8")
            if len(patched) > 0xFFFF:
                raise ValueError("patched UTF-8 constant is too long")
            out.extend(struct.pack(">H", len(patched)))
            out.extend(patched)
        elif tag in (3, 4):  # Integer / Float
            out.extend(data[pos:pos + 4])
            pos += 4
        elif tag in (5, 6):  # Long / Double occupy two CP slots
            out.extend(data[pos:pos + 8])
            pos += 8
            index += 1
        elif tag in (7, 8, 16, 19, 20):
            out.extend(data[pos:pos + 2])
            pos += 2
        elif tag in (9, 10, 11, 12, 17, 18):
            out.extend(data[pos:pos + 4])
            pos += 4
        elif tag == 15:
            out.extend(data[pos:pos + 3])
            pos += 3
        else:
            raise ValueError(f"unsupported constant-pool tag {tag} at index {index}")

        index += 1

    out.extend(data[pos:])
    return bytes(out)


def main() -> int:
    if len(sys.argv) != 3:
        print("usage: prepare_original.py <original-neoforge.jar> <output-dir>", file=sys.stderr)
        return 2

    original_jar = Path(sys.argv[1])
    output_root = Path(sys.argv[2])
    resources = output_root / "resources"
    classes = output_root / "classes"

    if output_root.exists():
        shutil.rmtree(output_root)
    resources.mkdir(parents=True)
    classes.mkdir(parents=True)

    with zipfile.ZipFile(original_jar) as jar:
        names = set(jar.namelist())

        # Client resources: textures, item/model JSON, language, particles, icon.
        for name in sorted(names):
            if not name.startswith("assets/hpm/") or name.endswith("/"):
                continue
            target = resources / name
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(jar.read(name))

        # Preserve only the original geometry classes required by our renderers.
        for name in MODEL_CLASSES:
            if name not in names:
                raise FileNotFoundError(f"original mod is missing required model class: {name}")
            patched = patch_constant_pool_utf8(jar.read(name))
            if OLD_NAME.encode() in patched:
                raise RuntimeError(f"ResourceLocation reference survived patching in {name}")
            target = classes / name
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_bytes(patched)

    copied_assets = sum(1 for p in resources.rglob("*") if p.is_file())
    print(f"Recovered {copied_assets} original asset files and {len(MODEL_CLASSES)} model classes")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
