package com.zyon.virtualpc.ui;

/** Conteúdo de uma janela do desktop. */
interface WinContent {
    /** Chamado quando a janela fecha (parar timers, salvar). */
    default void onClose() {}
    /** Chamado quando o app vai pro fundo (salvar). */
    default void onSave() {}
}
