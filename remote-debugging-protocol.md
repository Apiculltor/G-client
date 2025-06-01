# Protocolo de Debugging Remoto

## Durante o Teste

### 1. Verificação de Conexão dos Dispositivos
```bash
adb devices
# Deve mostrar:
# [SERIAL_BLADE] device
# [SERIAL_S24] device
```

### 2. Deploy Sequencial
```bash
# Primeiro o S24 (hub de processamento)
adb -s [S24_SERIAL] install -r app-s24-debug.apk

# Depois o Blade (cliente)
adb -s [BLADE_SERIAL] install -r app-blade-debug.apk
```

### 3. Monitoramento em Tempo Real
```bash
# Terminal 1: Logs do Blade
adb -s [BLADE_SERIAL] logcat -s VuzixApp

# Terminal 2: Logs do S24
adb -s [S24_SERIAL] logcat -s S24App

# Terminal 3: Connectivity SDK
adb logcat -s ConnectivitySDK
```

### 4. Cenários de Teste Prioritários

#### Teste 1: Conexão Base
- [ ] Vuzix Companion App instalado no S24
- [ ] Pairing entre Blade e S24 funcional
- [ ] Connectivity.isLinked() retorna true

#### Teste 2: Captura A/V
- [ ] Permissões de câmera/microfone no Blade
- [ ] Camera1 API inicializa corretamente
- [ ] MediaRecorder funciona

#### Teste 3: Comunicação
- [ ] Intent enviado do Blade para S24
- [ ] BroadcastReceiver recebe no S24
- [ ] Resposta retorna para o Blade

#### Teste 4: Processamento
- [ ] Dados A/V chegam no S24
- [ ] TensorFlow Lite/MediaPipe processa
- [ ] Resultado retorna para o Blade

## Problemas Comuns e Soluções

### Blade não conecta via ADB
```bash
# Verificar se developer options estão ativadas
# 7 taps em "About" no Settings do Blade
# Habilitar USB Debugging
```

### Connectivity SDK não funciona
- Verificar se Companion App está atualizado
- Re-fazer pairing entre dispositivos
- Verificar se ambos estão na mesma rede WiFi

### Erro de API Level
- Confirmar target SDK correto para cada dispositivo
- Verificar dependências dos SDKs Vuzix
