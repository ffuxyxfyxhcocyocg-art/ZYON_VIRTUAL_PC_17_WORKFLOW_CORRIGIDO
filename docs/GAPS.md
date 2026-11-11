# O que ainda falta (vs MASTER PROMPT)

## Só no teu PC / device (não existe como texto)

1. Binários QEMU `qemu-system-x86_64` e `qemu-system-aarch64` cross-compilados NDK → `jniLibs/arm64-v8a/`
2. Firmware OVMF_CODE.fd, OVMF_VARS.fd, bios-256k.bin → `assets/`
3. ISOs de guest (Ubuntu, Windows, etc.)
4. APK assinado instalado e testes §27 no hardware
5. `gradlew` binário (Android Studio gera no primeiro sync)
6. `local.properties` com caminho do SDK

## Código host — coberto no zip

- 40 módulos C++ (§26)
- QemuBackend argv + fork
- QMP, disco, snapshot, backup, rede, áudio, display, USB, 9p, power, security, perf, automation, plugin, config, API
- UI: Main, Create, Console, Hardware, Snapshots, Disks, Settings, Logs, Performance
- Clamp 16 GB, docs, script build

## Limitação física Android

- Sem KVM → só TCG (lento)
- Bridge/TAP precisa root
- Desktop do guest = SO real, não UI fake (correto pelo prompt §8)



## Experiência PC gamer e Windows

- O Game Center abre jogos Android instalados no próprio Android; não converte jogos Android em jogos Windows.
- Windows virtual real depende de binários QEMU arm64 compatíveis, firmware OVMF/SeaBIOS, ISO licenciada do Windows e disco virtual. Esses itens não estão incluídos no ZIP.
- Em Android sem KVM, Windows x86_64 usa TCG e pode ficar muito lento; desempenho depende do aparelho e do jogo.
