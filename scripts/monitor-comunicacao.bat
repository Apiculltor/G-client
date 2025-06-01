@echo off
echo ============================================
echo    MONITOR DE COMUNICAÇÃO EM TEMPO REAL
echo ============================================
echo.
echo [CTRL+C para parar o monitoramento]
echo.

echo Iniciando logs simultâneos...
echo.

REM Criar janelas separadas para cada log
start "VUZIX BLADE LOGS" cmd /k "echo === VUZIX BLADE LOGS === & adb -s emulator-5554 logcat -s VuzixBladeApp:* ConnectivitySDK:* BluetoothAdapter:*"

start "GALAXY S24 LOGS" cmd /k "echo === GALAXY S24 ULTRA LOGS === & adb -s emulator-5556 logcat -s VuzixBladeApp:* ConnectivitySDK:* BluetoothAdapter:*"

echo.
echo ============================================
echo   LOGS SENDO EXIBIDOS EM JANELAS SEPARADAS
echo   
echo   🔍 Procure por:
echo   - Conexões Bluetooth/WiFi
echo   - Mensagens do ConnectivitySDK
echo   - Erros de serialização
echo   - Timeouts de comunicação
echo ============================================
pause
