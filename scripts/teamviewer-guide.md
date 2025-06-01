# 🎯 GUIA COMPLETO - TESTE REMOTO VIA TEAMVIEWER

## 📋 PREPARAÇÃO PRÉ-SESSÃO (VOCÊ - LOCAL)

### ✅ 1. Verificação Final do Projeto
```bash
# Execute no seu computador ANTES da sessão TeamViewer
scripts\pre-build-check.bat
```

### ✅ 2. Build Final dos APKs
```bash
# Gerar APKs otimizados
scripts\build-release.bat
```

### ✅ 3. Preparar Scripts de Deploy
Todos os scripts já estão prontos em `scripts/`:
- `deploy-remote.bat` - Deploy automático
- `monitor-logs.bat` - Monitoramento em tempo real
- `test-scenarios.bat` - Cenários de teste

## 🔗 PROTOCOLO DE SESSÃO TEAMVIEWER

### **FASE 1: Conexão (5 minutos)**

1. **Seu sócio inicia TeamViewer** no computador dele
2. **Você conecta remotamente** usando o ID/senha
3. **Teste básico de controle** - mover mouse, abrir prompt

### **FASE 2: Verificação de Hardware (5 minutos)**

```bash
# 1. Abrir Prompt de Comando (como Admin)
cmd

# 2. Verificar dispositivos conectados
adb devices

# Resultado esperado:
# List of devices attached
# [SERIAL_BLADE]    device
# [S24_SERIAL]      device
```

**Se dispositivos não aparecerem:**
- Verificar cabo USB
- Habilitar "Depuração USB" nos dispositivos
- Reinstalar drivers ADB

### **FASE 3: Deploy Automático (10 minutos)**

```bash
# Navegar para pasta do projeto
cd C:\caminho\para\VuzixBladeApp

# Executar deploy automático
scripts\deploy-remote.bat
```

**O script fará automaticamente:**
- ✅ Detectar dispositivos
- ✅ Gerar APKs (se necessário)
- ✅ Instalar no Vuzix Blade
- ✅ Instalar no Samsung S24
- ✅ Iniciar aplicações

### **FASE 4: Monitoramento e Teste (10 minutos)**

```bash
# Em outro terminal, iniciar logs
scripts\monitor-logs.bat

# Executar cenários de teste
scripts\test-scenarios.bat
```

## 🧪 CENÁRIOS DE TESTE PRIORITÁRIOS

### **Teste 1: Conectividade Básica** ⏱️ 2 min
- [ ] Apps iniciaram sem crash
- [ ] Logs mostram inicialização correta
- [ ] Interface aparece nos dispositivos

### **Teste 2: Comunicação Blade ↔ S24** ⏱️ 3 min
- [ ] Vuzix Companion App instalado no S24
- [ ] Pairing realizado com sucesso
- [ ] Teste de envio de mensagem simples

### **Teste 3: Captura A/V** ⏱️ 3 min
- [ ] Permissões de câmera concedidas
- [ ] Captura de vídeo funciona
- [ ] Áudio é gravado simultaneamente

### **Teste 4: Processamento** ⏱️ 2 min
- [ ] Dados chegam no S24
- [ ] Processamento é executado
- [ ] Feedback retorna para o Blade

## 🚨 SOLUÇÃO DE PROBLEMAS COMUNS

### **Problema: Dispositivos não detectados**
```bash
# Verificar drivers
adb kill-server
adb start-server
adb devices
```

### **Problema: APK não instala**
```bash
# Desinstalar versão anterior
adb uninstall com.seudominio.vuzixbladeapp
adb install app\build\outputs\apk\debug\app-debug.apk
```

### **Problema: App crasha ao iniciar**
```bash
# Verificar logs específicos
adb logcat -s AndroidRuntime:E
```

### **Problema: Connectivity SDK não funciona**
1. Verificar se Companion App está instalado no S24
2. Refazer pairing entre dispositivos
3. Verificar se ambos estão na mesma rede WiFi

## 📞 COMUNICAÇÃO DURANTE O TESTE

### **Canal de Voz**
- WhatsApp ou Telegram para comunicação rápida
- Microfone do TeamViewer como backup

### **Relatório em Tempo Real**
Seu sócio deve informar:
- ✅ "App iniciou no Blade"
- ✅ "Pairing realizado"
- ✅ "Gravação funcionou"
- ❌ "Erro: [descrição]"

### **Screenshots/Vídeos**
- Solicitar fotos da tela do Blade
- Gravar vídeo do funcionamento se possível

## ⏰ CRONOGRAMA OTIMIZADO (30 MIN TOTAL)

| Tempo | Ação | Responsável |
|-------|------|-------------|
| 0-5 min | Conexão TeamViewer + Verificação Hardware | Ambos |
| 5-15 min | Deploy + Primeira inicialização | Você (remoto) |
| 15-25 min | Testes funcionais | Sócio (local) |
| 25-30 min | Debug de problemas + Próximos passos | Ambos |

## 📝 CHECKLIST PÓS-TESTE

### ✅ Dados para Coletar
- [ ] Logs de erro (se houver)
- [ ] Screenshots de funcionamento
- [ ] Lista de funcionalidades testadas
- [ ] Problemas identificados
- [ ] Sugestões de melhoria

### ✅ Próximos Passos
- [ ] Definir correções necessárias
- [ ] Agendar próxima sessão de teste
- [ ] Preparar versão corrigida
- [ ] Documentar lições aprendidas

## 🔧 BACKUP - SE TEAMVIEWER FALHAR

### **Opção 1: ADB over Network**
```bash
# No computador do sócio (uma vez)
adb tcpip 5555
adb connect [IP_LOCAL]:5555

# No seu computador
adb connect [IP_PUBLICO_SOCIO]:5555
```

### **Opção 2: Google Drive**
- Fazer upload dos APKs
- Sócio baixa e instala manualmente
- Logs enviados via WhatsApp/email

### **Opção 3: GitHub Release**
- Push código + APKs para GitHub
- Sócio clona e executa localmente

## 📞 CONTATOS DE EMERGÊNCIA
- Suporte Vuzix: [link]
- Documentação oficial: [link]
- Comunidade desenvolvedores: [link]

---

**🎯 OBJETIVO:** Validar funcionamento básico da integração Blade+S24 e identificar próximos passos de desenvolvimento.

**⚡ FOCO:** Eficiência máxima em 30 minutos de sessão remota.
