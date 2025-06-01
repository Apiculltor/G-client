# Checklist Pré-Teste Hardware

## ✅ Preparação do Código
- [ ] Build dos APKs finalizados sem erros
- [ ] Versionamento correto (API 22 para Blade, API 34+ para S24)
- [ ] Permissões configuradas no AndroidManifest.xml
- [ ] SDKs Vuzix integrados corretamente

## ✅ Preparação dos APKs
```bash
# Gerar APKs de debug
./gradlew assembleDebug

# Localização dos APKs:
# app-blade/build/outputs/apk/debug/app-blade-debug.apk
# app-s24/build/outputs/apk/debug/app-s24-debug.apk
```

## ✅ Scripts de Deploy
- [ ] Script de instalação automática criado
- [ ] Script de logging criado
- [ ] Comandos ADB testados localmente

## ✅ Documentação de Teste
- [ ] Cenários de teste definidos
- [ ] Sequência de ações documentada
- [ ] Pontos de verificação listados

## ✅ Comunicação com Sócio
- [ ] Horário de teste agendado
- [ ] Software de controle remoto testado
- [ ] Plano B definido caso algo falhe
