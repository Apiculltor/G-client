@echo off
echo ============================================
echo    INICIANDO TESTE COM AMBOS EMULADORES
echo ============================================
echo.

echo [1/4] Verificando emuladores disponíveis...
call emulator -list-avds
echo.

echo [2/4] Iniciando Emulador Vuzix Blade (API 22)...
start "Vuzix Blade" emulator -avd Vuzix_Blade_API22 -port 5554
echo Aguardando inicialização...
timeout /t 10 /nobreak
echo.

echo [3/4] Iniciando Emulador Galaxy S24 Ultra (API 34)...
start "Galaxy S24 Ultra" emulator -avd Galaxy_S24_Ultra_API34 -port 5556
echo Aguardando inicialização...
timeout /t 10 /nobreak
echo.

echo [4/4] Verificando dispositivos conectados...
adb devices
echo.

echo ============================================
echo   AMBOS EMULADORES INICIADOS!
echo   
echo   Vuzix Blade:     localhost:5554
echo   Galaxy S24:      localhost:5556
echo   
echo   PRÓXIMO PASSO: Instalar APKs nos dois
echo ============================================
pause
