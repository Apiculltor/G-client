@echo off
:: ===================================================================
:: SCRIPT DE DEPLOY REMOTO - VUZIX BLADE + SAMSUNG S24 ULTRA
:: Versão: 1.0 | Data: 01/06/2025
:: ===================================================================

echo.
echo ========================================
echo   VUZIX BLADE - DEPLOY REMOTO
echo ========================================
echo.

:: Verificar se ADB está disponível
adb version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ ADB não encontrado! Verifique se Android SDK está instalado.
    pause
    exit /b 1
)

:: Verificar dispositivos conectados
echo 📱 Verificando dispositivos conectados...
adb devices
echo.

:: Perguntar ao usuário quais dispositivos usar
echo Digite os seriais dos dispositivos:
set /p BLADE_SERIAL=🥽 Vuzix Blade Serial: 
set /p S24_SERIAL=📱 Samsung S24 Serial: 

if "%BLADE_SERIAL%"=="" (
    echo ⚠️  Serial do Blade não informado, tentando detectar automaticamente...
    for /f "tokens=1" %%i in ('adb devices ^| findstr "device" ^| findstr /v "List"') do set BLADE_SERIAL=%%i
)

if "%S24_SERIAL%"=="" (
    echo ⚠️  Serial do S24 não informado, tentando detectar automaticamente...
    for /f "tokens=1" %%i in ('adb devices ^| findstr "device" ^| findstr /v "List"') do set S24_SERIAL=%%i
)

echo.
echo 🎯 Dispositivos selecionados:
echo    Blade: %BLADE_SERIAL%
echo    S24:   %S24_SERIAL%
echo.

:: Gerar APKs
echo 🔨 Gerando APKs de debug...
call gradlew.bat assembleDebug
if %errorlevel% neq 0 (
    echo ❌ Erro ao gerar APKs!
    pause
    exit /b 1
)

:: Localizar APKs
set BLADE_APK=app\build\outputs\apk\debug\app-debug.apk
set S24_APK=app\build\outputs\apk\debug\app-debug.apk

if not exist "%BLADE_APK%" (
    echo ❌ APK do Blade não encontrado: %BLADE_APK%
    pause
    exit /b 1
)

echo ✅ APKs gerados com sucesso!
echo.

:: Deploy no Vuzix Blade
echo 🥽 Instalando no Vuzix Blade...
if not "%BLADE_SERIAL%"=="" (
    adb -s %BLADE_SERIAL% install -r "%BLADE_APK%"
    if %errorlevel% equ 0 (
        echo ✅ App instalado no Blade!
    ) else (
        echo ❌ Erro ao instalar no Blade
    )
) else (
    echo ⚠️  Blade não detectado, tentando instalação genérica...
    adb install -r "%BLADE_APK%"
)

echo.

:: Deploy no Samsung S24 Ultra (mesmo APK, mas poderia ser diferente)
echo 📱 Instalando no Samsung S24 Ultra...
if not "%S24_SERIAL%"=="" (
    adb -s %S24_SERIAL% install -r "%S24_APK%"
    if %errorlevel% equ 0 (
        echo ✅ App instalado no S24!
    ) else (
        echo ❌ Erro ao instalar no S24
    )
) else (
    echo ⚠️  S24 não detectado, pulando instalação...
)

echo.
echo ========================================
echo   INICIALIZANDO APLICAÇÕES
echo ========================================

:: Iniciar app no Blade
echo 🚀 Iniciando app no Vuzix Blade...
if not "%BLADE_SERIAL%"=="" (
    adb -s %BLADE_SERIAL% shell am start -n com.seudominio.vuzixbladeapp/.MainActivity
    if %errorlevel% equ 0 (
        echo ✅ App iniciado no Blade!
    )
)

:: Aguardar um pouco
timeout /t 2 /nobreak >nul

:: Iniciar app no S24 (se existir versão separada)
echo 🚀 Iniciando app no Samsung S24...
if not "%S24_SERIAL%"=="" (
    adb -s %S24_SERIAL% shell am start -n com.seudominio.vuzixbladeapp/.MainActivity
    if %errorlevel% equ 0 (
        echo ✅ App iniciado no S24!
    )
)

echo.
echo ========================================
echo   DEPLOY CONCLUÍDO!
echo ========================================
echo.
echo 🎉 Apps instalados e iniciados nos dispositivos
echo 📋 Próximos passos:
echo    1. Verificar se Companion App está instalado no S24
echo    2. Fazer pairing entre Blade e S24
echo    3. Testar comunicação básica
echo    4. Executar cenários de teste
echo.
echo 📊 Para monitorar logs, execute:
echo    scripts\monitor-logs.bat
echo.

pause
