# Como executar a compilação do ZYON no GitHub

## Importante
O ZIP não inicia uma execução sozinho. É necessário enviar os arquivos para um repositório GitHub e executar o workflow pelo GitHub Actions.

## Envio correto
1. Extraia este ZIP.
2. Abra a pasta `zyon_build`.
3. Envie **todo o conteúdo que está dentro dela** para a raiz do repositório GitHub (não envie uma pasta `zyon_build` dentro do repositório).
4. Confirme que o arquivo aparece exatamente em `.github/workflows/build.yml` na raiz do repositório.
5. Faça commit e push para `main` ou `master`.

## Execução manual
1. No GitHub, abra o repositório.
2. Entre em **Actions**.
3. Selecione **Build ZYON VIRTUAL PC 17**.
4. Clique em **Run workflow**.
5. Escolha `main` (ou `master`, conforme a branch usada) e confirme.
6. Abra a execução criada e acompanhe os passos.
7. Quando terminar com sucesso, abra **Artifacts** e baixe `zyon-virtual-pc-17-debug-apk`.

## Se falhar
Abra a etapa vermelha, copie o primeiro erro completo (não apenas a mensagem final) e compartilhe para diagnóstico.

## Limitação importante do projeto atual
A compilação do APK não significa que o Windows/QEMU já esteja pronto para executar. No ZIP recebido, `app/src/main/jniLibs/arm64-v8a/` contém apenas um README, sem bibliotecas QEMU (`libqemu*.so`), e a pasta de firmware também contém apenas instruções. Portanto, mesmo que o APK compile, a virtualização real ainda depende de adicionar os binários QEMU ARM64 e firmwares compatíveis e testá-los no aparelho.
