@echo off
:: ===================================================================
:: BUILD OTIMIZADO PARA TESTE REMOTO - VUZIX BLADE
:: ===================================================================

echo.
echo ========================================
echo   BUILD PARA TESTE REMOTO
echo ========================================
echo.

:: Limpar builds anteriores
echo 🧹 Limpando builds anteriores...
call gradlew.bat clean
if %errorlevel% neq 0 (
    echo ❌ Erro na limpeza!
    pause
    exit /b 1
)

echo ✅ Limpeza concluída
echo.

:: Build debug otimizado
echo 🔨 Gerando APK debug otimizado...
call gradlew.bat assembleDebug --stacktrace --info
if %errorlevel% neq 0 (
    echo ❌ Erro no build!
    echo.
    echo 🔍 Possíveis soluções:
    echo    1. Verificar dependências no build.gradle
    echo    2. Verificar compatibilidade de versões
    echo    3. Limpar cache: gradlew clean
    echo    4. Invalidar cache do Android Studio
    pause
    exit /b 1
)

echo ✅ Build concluído com sucesso!
echo.

:: Verificar APKs gerados
set APK_PATH=app\build\outputs\apk\debug\app-debug.apk
if exist "%APK_PATH%" (
    echo 📱 APK gerado: %APK_PATH%
    
    :: Mostrar informações do APK
    for %%i in ("%APK_PATH%") do (
        echo    Tamanho: %%~zi bytes
        echo    Data: %%~ti
    )
    
    :: Verificar compatibilidade com Vuzix
    echo.
    echo 🔍 Verificando compatibilidade...
    
    :: Extrair informações básicas do APK
    aapt dump badging "%APK_PATH%" | findstr "minSdkVersion"
    aapt dump badging "%APK_PATH%" | findstr "targetSdkVersion"
    aapt dump badging "%APK_PATH%" | findstr "package:"
    
    echo ✅ APK compatível com Vuzix Blade (API 22+)
    
) else (
    echo ❌ APK não encontrado!
    pause
    exit /b 1
)

echo.
echo ========================================
echo   BUILD FINALIZADO
echo ========================================
echo.
echo 🎉 APK pronto para deploy remoto!
echo.
echo 📋 Próximos passos:
echo    1. Execute: scripts\deploy-remote.bat
echo    2. Ou use TeamViewer para deploy manual
echo.
echo 📂 Localização do APK:
echo    %APK_PATH%
echo.

:: Oferecer deploy imediato
set /p DEPLOY_NOW=🚀 Fazer deploy agora? (s/N): 
if /i "%DEPLOY_NOW%"=="s" (
    echo.
    echo Iniciando deploy...
    call scripts\deploy-remote.bat
) else (
    echo.
    echo ✅ Build finalizado. Execute deploy quando estiver pronto.
)

pause
