# ZYON VIRTUAL PC 17 — edição sem Key

Esta edição remove a tela de licença, a validação por Key, o cliente de licença,
o armazenamento de tokens e a configuração do endpoint de licença. O launcher
abre o aplicativo sem autenticação por licença.

## Abrir no Android Studio
1. Extraia o ZIP.
2. Abra a pasta `ZYON_VIRTUAL_PC_17` no Android Studio.
3. Aguarde a sincronização do Gradle.
4. Configure Android SDK/NDK e compile o projeto.

## Estado técnico — limitações não ocultadas
O ZIP de origem contém estrutura Android e módulos C++, mas a integração QEMU
não está pronta para ser considerada funcional: os binários QEMU não estão
incluídos em `app/src/main/jniLibs/arm64-v8a/`, e firmware e imagens de SO
também não estão incluídos. A ponte JNI original tinha rotinas provisórias;
nesta edição, operações de VM ainda não implementadas retornam erro em vez de
fingir sucesso.

Portanto, esta entrega remove a licença e atualiza a versão para 17, mas **não
declara que Windows/Linux convidado, renderização, rede ou entrada já foram
concluídos ou testados**. Não há APK de release validado nesta entrega. Para
virtualização real, é necessário compilar/integrar QEMU para Android/arm64,
fornecer firmware compatível e concluir a integração JNI/QMP/renderer com testes
em aparelho físico.

## Limitações
- Em Android sem KVM, x86/x86_64 normalmente depende de TCG e pode ser lento.
- TAP/bridge pode exigir root; NAT/SLIRP é mais adequado para uso comum.
- ISOs e discos de sistemas operacionais devem ser fornecidos legalmente pelo usuário.
