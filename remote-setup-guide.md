# Guia de Setup Remoto - Vuzix Blade Project

## Setup no Computador do Sócio

### 1. Instalar Android Studio
- Download: https://developer.android.com/studio
- Instalar SDK API 22 (Lollipop) para Vuzix Blade
- Instalar SDK API 34+ para Samsung S24 Ultra

### 2. Configurar Git e Clonar Projeto
```bash
git clone [SEU_REPOSITORIO]
cd vuzix-s24-project
```

### 3. Configurar Dependências Vuzix
```gradle
// No build.gradle (project level)
allprojects {
    repositories {
        google()
        mavenCentral()
        maven { url "https://jitpack.io" }
    }
}

// No build.gradle (app level - Blade)
dependencies {
    implementation 'com.vuzix:hud-actionmenu:1.6.0'
    implementation 'com.vuzix:connectivity-sdk:master-SNAPSHOT'
}
```

### 4. Scripts de Deploy Automático
```bash
# deploy-both.sh
#!/bin/bash
echo "Deploying to Vuzix Blade..."
adb -s [BLADE_SERIAL] install -r app-blade/build/outputs/apk/debug/app-blade-debug.apk

echo "Deploying to Samsung S24..."
adb -s [S24_SERIAL] install -r app-s24/build/outputs/apk/debug/app-s24-debug.apk

echo "Starting apps..."
adb -s [BLADE_SERIAL] shell am start -n com.yourpackage.blade/.MainActivity
adb -s [S24_SERIAL] shell am start -n com.yourpackage.s24/.MainActivity
```

### 5. Debugging Remoto
```bash
# Logs em tempo real
adb -s [BLADE_SERIAL] logcat -s VuzixBlade &
adb -s [S24_SERIAL] logcat -s S24Ultra &
```
