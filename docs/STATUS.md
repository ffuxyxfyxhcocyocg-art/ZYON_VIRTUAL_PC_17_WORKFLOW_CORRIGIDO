# ZYON VIRTUAL PC 17 — Status (atualizado)

## Implementado — fonte completa

### Desktop e janelas
- Desktop completo estilo Windows 11
- Gerenciador de janelas: arrastar, minimizar, maximizar, fechar, redimensionar
- **Snap Layouts** (segurar botão maximizar → escolher zona)
- **ALT+TAB overlay** (via teclado virtual ou físico)
- Múltiplas janelas simultâneas com z-order e foco
- Barra de tarefas com relógio, bateria e apps abertos
- Menu Iniciar com pesquisa de apps e ferramentas

### Teclado e Mouse
- **Teclado virtual de PC completo** (VirtualKeyboardView)
  ESC, F1-F12, CTRL, ALT, SHIFT, WIN, setas, numpad, combinações
- **Mouse virtual** (VirtualMouseView)
  Movimento, clique esquerdo (tap), clique direito (long press), arrasto
- Botões flutuantes para abrir/fechar teclado e mouse
- Suporte a teclado/mouse físico via Android

### Configurações
- SettingsActivity completa com menu de navegação
- **ResolutionSettingsActivity** — 6 resoluções padrão + salva em prefs
- Tema escuro/claro

### PC Gamer
- GameLauncherActivity com biblioteca de jogos Android instalados

### QEMU / VM
- 40 módulos C++ (backend completo)
- QemuBackend: argv + fork/exec
- QmpClient: Unix socket QMP
- DiskManager, SnapshotManager, BackupManager
- Todos os managers: CPU, RAM, rede, áudio, display, firmware
- JNI bridge + ZyonNative

## Externo (você fornece)
- Binários QEMU arm64 → jniLibs/arm64-v8a/
- OVMF / SeaBIOS → assets/firmware/
- ISOs de guest

## Limitações conhecidas do Android
- Sem KVM = TCG (lento para Windows x86_64)
- Bridge/TAP requer root
- Renderização do guest via Surface (VmConsoleActivity)
