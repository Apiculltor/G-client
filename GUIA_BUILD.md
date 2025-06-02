# 🔧 GUIA DE BUILD - ANDROID STUDIO

## 📋 SEQUÊNCIA PARA FAZER BUILD DO PROJETO VUZIX

### 🚀 PASSO A PASSO COMPLETO:

#### 1️⃣ **ABRIR PROJETO**
```
File → Open → Selecionar pasta VuzixBladeApp
```

#### 2️⃣ **SYNC GRADLE (OBRIGATÓRIO)**
```
Clique no ícone 🔄 "Sync Project with Gradle Files"
```
**Aguarde terminar!** (pode demorar alguns minutos)

#### 3️⃣ **VERIFICAR SE NÃO HÁ ERROS**
- Verifique na aba **"Build"** se não há erros
- Se houver erros, pare aqui e reporte

#### 4️⃣ **FAZER BUILD**
```
Menu: Build → Rebuild Project
```
**OU via terminal:**
```cmd
gradlew.bat clean assembleDebug
```

#### 5️⃣ **VERIFICAR BUILD SUCESSO**
Deve aparecer: 
```
BUILD SUCCESSFUL in X seconds
```

#### 6️⃣ **LOCALIZAR APK GERADO**
```
VuzixBladeApp/app/build/outputs/apk/debug/app-debug.apk
```

## ⚡ MÉTODO RÁPIDO - LINHA DE COMANDO:

```cmd
# Abrir CMD na pasta do projeto
cd C:\[CAMINHO]\VuzixBladeApp

# Executar build completo
gradlew.bat clean assembleDebug
```

## 🚨 SE DER ERRO:

### Erro de "SDK not found":
```cmd
# Verificar se Android SDK está configurado
# Android Studio → File → Settings → Android SDK
```

### Erro de "Gradle Wrapper":
```cmd
# Recriar wrapper
gradlew.bat wrapper
```

### Erro de "Permission Denied":
```cmd
# Dar permissão ao gradlew (se no Linux/Mac)
chmod +x gradlew
```

## ✅ SINAIS DE SUCESSO:

1. **Sync Gradle:** "Gradle sync finished"
2. **Build:** "BUILD SUCCESSFUL"
3. **APK gerado:** Arquivo existe em `app/build/outputs/apk/debug/`

## 📱 APÓS BUILD BEM-SUCEDIDO:

### Instalar nos dispositivos:
```cmd
# Verificar dispositivos conectados
adb devices

# Instalar no Galaxy S24
adb -s [DEVICE_ID_S24] install app/build/outputs/apk/debug/app-debug.apk

# Instalar no Vuzix Blade
adb -s [DEVICE_ID_VUZIX] install app/build/outputs/apk/debug/app-debug.apk
```

## 🔍 DICAS IMPORTANTES:

1. **Sempre fazer Sync primeiro** antes do Build
2. **Aguardar terminar** cada etapa completamente
3. **Verificar logs** na aba Build para erros
4. **Internet necessária** para download de dependências

---
**📞 SE PRECISAR DE AJUDA:** 
Compartilhe o erro completo da aba Build ou terminal.