@echo off
:: ===================================================================
:: MONITOR DE LOGS EM TEMPO REAL - VUZIX BLADE + SAMSUNG S24
:: ===================================================================

echo.
echo ========================================
echo   MONITOR DE LOGS - TESTE REMOTO
echo ========================================
echo.

:: Verificar dispositivos
adb devices
echo.

set /p BLADE_SERIAL=🥽 Vuzix Blade Serial (Enter para auto-detectar): 
set /p S24_SERIAL=📱 Samsung S24 Serial (Enter para auto-detectar): 

:: Auto-detectar se não informado
if "%BLADE_SERIAL%"=="" (
    for /f "tokens=1" %%i in ('adb devices ^| findstr "device" ^| findstr /v "List" ^| head -1') do set BLADE_SERIAL=%%i
)

if "%S24_SERIAL%"=="" (
    for /f "tokens=1" %%i in ('adb devices ^| findstr "device" ^| findstr /v "List" ^| tail -1') do set S24_SERIAL=%%i
)

echo.
echo 📊 Monitorando logs de:
echo    Blade: %BLADE_SERIAL%
echo    S24:   %S24_SERIAL%
echo.
echo 🚀 Iniciando monitoramento... (Ctrl+C para parar)
echo ========================================
echo.

:: Limpar logs anteriores
if not "%BLADE_SERIAL%"=="" adb -s %BLADE_SERIAL% logcat -c
if not "%S24_SERIAL%"=="" adb -s %S24_SERIAL% logcat -c

:: Criar arquivo de log com timestamp
set LOG_FILE=logs\remote-test-%date:~-4,4%%date:~-10,2%%date:~-7,2%-%time:~0,2%%time:~3,2%%time:~6,2%.txt
mkdir logs 2>nul
echo Log iniciado em %date% %time% > "%LOG_FILE%"

:: Monitorar logs filtrados
echo 🔍 Filtros ativos: VuzixBladeApp, ConnectivitySDK, Camera, MediaRecorder
echo.

(
if not "%BLADE_SERIAL%"=="" (
    echo === BLADE LOGS ===
    adb -s %BLADE_SERIAL% logcat -s VuzixBladeApp:V ConnectivitySDK:V Camera:V MediaRecorder:V AndroidRuntime:E
) 
if not "%S24_SERIAL%"=="" (
    echo.
    echo === S24 LOGS ===
    adb -s %S24_SERIAL% logcat -s VuzixBladeApp:V ConnectivitySDK:V TensorFlowLite:V MediaPipe:V AndroidRuntime:E
)
) | tee -a "%LOG_FILE%"

echo.
echo 📄 Logs salvos em: %LOG_FILE%
pause
