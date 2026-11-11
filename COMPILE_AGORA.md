# Compilar — ZYON VIRTUAL PC 17

1. Android Studio → File → Open → pasta `ZyonVirtualPC17`
2. SDK 34, NDK 26.x, CMake 3.22.1, JDK 17 (o Studio oferece instalar)
3. `local.properties` na raiz: `sdk.dir=/caminho/do/Sdk`
4. (Opcional) `python tools/setup_secret.py` pra gerar o SEU segredo de keys
5. Build → Build APK(s). Release precisa de keystore sua.

## O que você vê
Tela de key → boot → **desktop** com Iniciar, Explorador, Bloco de Notas, Gerenciador de Tarefas,
Apps do Celular e Máquinas Virtuais.

## Windows/Linux de verdade
Precisa dos binários do QEMU em `app/src/main/jniLibs/arm64-v8a/` (libqemu*.so),
firmware em `assets/firmware` e uma ISO. Sem isso o app roda como desktop de apps.
