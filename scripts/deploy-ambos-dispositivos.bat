@echo off
echo ============================================
echo    DEPLOY NOS DOIS EMULADORES
echo ============================================
echo.

echo [1/6] Compilando APK Debug...
call gradlew assembleDebug
if %ERRORLEVEL% neq 0 (
    echo ERRO: Falha na compilação!
    pause
    exit /b 1
)
echo.

echo [2/6] Verificando dispositivos conectados...
adb devices
echo.

echo [3/6] Instalando no Vuzix Blade (5554)...
adb -s emulator-5554 install -r app\build\outputs\apk\debug\app-debug.apk
if %ERRORLEVEL% neq 0 (
    echo AVISO: Falha na instalação no Vuzix Blade
) else (
    echo ✓ Instalado no Vuzix Blade com sucesso!
)
echo.

echo [4/6] Instalando no Galaxy S24 Ultra (5556)...
adb -s emulator-5556 install -r app\build\outputs\apk\debug\app-debug.apk
if %ERRORLEVEL% neq 0 (
    echo AVISO: Falha na instalação no Galaxy S24 Ultra
) else (
    echo ✓ Instalado no Galaxy S24 Ultra com sucesso!
)
echo.

echo [5/6] Iniciando app no Vuzix Blade...
adb -s emulator-5554 shell am start -n com.seudominio.vuzixbladeapp/.MainActivity
echo.

echo [6/6] Iniciando app no Galaxy S24 Ultra...
adb -s emulator-5556 shell am start -n com.seudominio.vuzixbladeapp/.MainActivity
echo.

echo ============================================
echo   DEPLOY COMPLETO!
echo   
echo   ✓ App rodando no Vuzix Blade
echo   ✓ App rodando no Galaxy S24 Ultra
echo   
echo   AGORA TESTE A COMUNICAÇÃO ENTRE ELES!
echo ============================================
pause
