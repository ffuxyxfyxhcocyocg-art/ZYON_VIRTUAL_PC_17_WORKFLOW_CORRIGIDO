# Erro GitHub 422 — Update is not a fast forward

Este erro não é causado pelo AndroidManifest, Gradle ou código Android. Ele ocorre quando o ramo remoto `main` tem commits que não estão no ramo local.

## Pelo terminal do ambiente Android

Faça backup/commit das alterações locais e execute na pasta do projeto:

```bash
git fetch origin
git pull --rebase origin main
# Se houver conflitos: edite os arquivos, marque-os como resolvidos e execute:
git add .
git rebase --continue
# Quando o rebase terminar:
git push origin main
```

Não use `git push --force` como primeira tentativa. Se o ambiente não disponibilizar terminal Git ou rebase, sincronize/clonе o repositório remoto atualizado e copie suas alterações para essa cópia antes de enviar.
