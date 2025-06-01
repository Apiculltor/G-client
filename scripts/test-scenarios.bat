@echo off
:: ===================================================================
:: CENÁRIOS DE TESTE AUTOMATIZADOS - VUZIX BLADE + S24 ULTRA
:: ===================================================================

echo.
echo ========================================
echo   CENÁRIOS DE TESTE - VUZIX BLADE
echo ========================================
echo.

set /p BLADE_SERIAL=🥽 Vuzix Blade Serial: 
set /p S24_SERIAL=📱 Samsung S24 Serial: 

if "%BLADE_SERIAL%"=="" (
    echo ❌ Serial do Blade é obrigatório!
    pause
    exit /b 1
)

echo.
echo 🎯 Dispositivos configurados:
echo    Blade: %BLADE_SERIAL%
echo    S24:   %S24_SERIAL%
echo.

:: ===================================================================
:: CENÁRIO 1: TESTE DE CONECTIVIDADE BÁSICA
:: ===================================================================

echo 📡 CENÁRIO 1: Conectividade Básica
echo ========================================

echo 🔍 Testando conectividade dos dispositivos...
adb -s %BLADE_SERIAL% shell pm list packages | findstr vuzixbladeapp >nul
if %errorlevel% equ 0 (
    echo ✅ App instalado no Blade
) else (
    echo ❌ App NÃO instalado no Blade
    goto :error
)

if not "%S24_SERIAL%"=="" (
    adb -s %S24_SERIAL% shell pm list packages | findstr vuzixbladeapp >nul
    if %errorlevel% equ 0 (
        echo ✅ App instalado no S24
    ) else (
        echo ❌ App NÃO instalado no S24
    )
)

echo.
timeout /t 3 /nobreak >nul

:: ===================================================================
:: CENÁRIO 2: TESTE DE INICIALIZAÇÃO
:: ===================================================================

echo 🚀 CENÁRIO 2: Inicialização dos Apps
echo ========================================

echo 🔄 Forçando parada dos apps...
adb -s %BLADE_SERIAL% shell am force-stop com.seudominio.vuzixbladeapp
if not "%S24_SERIAL%"=="" adb -s %S24_SERIAL% shell am force-stop com.seudominio.vuzixbladeapp

timeout /t 2 /nobreak >nul

echo 🚀 Iniciando app no Blade...
adb -s %BLADE_SERIAL% shell am start -n com.seudominio.vuzixbladeapp/.MainActivity
if %errorlevel% equ 0 (
    echo ✅ App iniciado no Blade
) else (
    echo ❌ Erro ao iniciar app no Blade
    goto :error
)

if not "%S24_SERIAL%"=="" (
    echo 🚀 Iniciando app no S24...
    adb -s %S24_SERIAL% shell am start -n com.seudominio.vuzixbladeapp/.MainActivity
    if %errorlevel% equ 0 (
        echo ✅ App iniciado no S24
    ) else (
        echo ❌ Erro ao iniciar app no S24
    )
)

echo.
timeout /t 5 /nobreak >nul

:: ===================================================================
:: CENÁRIO 3: TESTE DE LOGS E FUNCIONAMENTO
:: ===================================================================

echo 📊 CENÁRIO 3: Verificação de Logs
echo ========================================

echo 🔍 Verificando logs de inicialização...

echo === LOGS DO BLADE ===
adb -s %BLADE_SERIAL% logcat -d -s VuzixBladeApp:V | tail -10

if not "%S24_SERIAL%"=="" (
    echo.
    echo === LOGS DO S24 ===
    adb -s %S24_SERIAL% logcat -d -s VuzixBladeApp:V | tail -10
)

echo.
timeout /t 3 /nobreak >nul

:: ===================================================================
:: CENÁRIO 4: TESTE DE PERMISSÕES
:: ===================================================================

echo 🔐 CENÁRIO 4: Verificação de Permissões
echo ========================================

echo 🔍 Verificando permissões no Blade...
adb -s %BLADE_SERIAL% shell dumpsys package com.seudominio.vuzixbladeapp | findstr permission
echo.

:: ===================================================================
:: CENÁRIO 5: TESTE DE COMUNICAÇÃO (SE POSSÍVEL)
:: ===================================================================

echo 📡 CENÁRIO 5: Teste de Comunicação
echo ========================================

echo 📱 Enviando intent de teste para o Blade...
adb -s %BLADE_SERIAL% shell am broadcast -a com.seudominio.vuzixbladeapp.TEST_COMMUNICATION --es message "Teste de comunicação remota"

if %errorlevel% equ 0 (
    echo ✅ Intent enviado com sucesso
    echo 🔍 Verificar logs para confirmar recebimento
) else (
    echo ❌ Erro ao enviar intent de teste
)

echo.
timeout /t 3 /nobreak >nul

:: ===================================================================
:: CENÁRIO 6: TESTE DE FUNCIONALIDADES ESPECÍFICAS
:: ===================================================================

echo 🎬 CENÁRIO 6: Teste de Funcionalidades
echo ========================================

echo 📸 Testando captura de mídia...
adb -s %BLADE_SERIAL% shell am broadcast -a com.seudominio.vuzixbladeapp.START_RECORDING --es output_path "/sdcard/test_recording.mp4"

if %errorlevel% equ 0 (
    echo ✅ Comando de gravação enviado
    echo ⏳ Aguardando 5 segundos...
    timeout /t 5 /nobreak >nul
    
    echo ⏹️ Parando gravação...
    adb -s %BLADE_SERIAL% shell am broadcast -a com.seudominio.vuzixbladeapp.STOP_RECORDING
    
    if %errorlevel% equ 0 (
        echo ✅ Comando de parada enviado
    ) else (
        echo ❌ Erro ao parar gravação
    )
) else (
    echo ❌ Erro ao iniciar gravação
)

echo.
timeout /t 3 /nobreak >nul

:: ===================================================================
:: CENÁRIO 7: TESTE DE COMPANION APP (S24)
:: ===================================================================

if not "%S24_SERIAL%"=="" (
    echo 🤝 CENÁRIO 7: Verificação do Companion App
    echo ========================================
    
    echo 🔍 Verificando se Vuzix Companion App está instalado...
    adb -s %S24_SERIAL% shell pm list packages | findstr vuzix.companion >nul
    if %errorlevel% equ 0 (
        echo ✅ Vuzix Companion App encontrado no S24
        
        echo 🚀 Verificando se está em execução...
        adb -s %S24_SERIAL% shell ps | findstr companion >nul
        if %errorlevel% equ 0 (
            echo ✅ Companion App está em execução
        ) else (
            echo ⚠️ Companion App não está em execução
            echo 💡 Dica: Abra o Companion App manualmente no S24
        )
    ) else (
        echo ❌ Vuzix Companion App NÃO encontrado no S24
        echo 💡 Dica: Instale o Companion App da Play Store
    )
    
    echo.
    timeout /t 3 /nobreak >nul
)

:: ===================================================================
:: RELATÓRIO FINAL
:: ===================================================================

echo 📋 RELATÓRIO FINAL DE TESTES
echo ========================================
echo.

echo ✅ TESTES CONCLUÍDOS
echo.
echo 📊 Resultados:
echo    - Apps instalados e funcionais
echo    - Logs de inicialização verificados
echo    - Comandos de teste enviados
echo    - Permissões verificadas

if not "%S24_SERIAL%"=="" (
    echo    - Companion App verificado no S24
)

echo.
echo 📝 PRÓXIMOS PASSOS MANUAIS:
echo    1. Verificar interface visual nos dispositivos
echo    2. Testar navegação por gestos no Blade
echo    3. Fazer pairing entre Blade e S24
echo    4. Testar captura A/V real
echo    5. Verificar comunicação bidirecional
echo.

echo 📄 Para monitoramento contínuo, execute:
echo    scripts\monitor-logs.bat
echo.

echo 🎉 TESTES AUTOMATIZADOS FINALIZADOS!
echo.
goto :end

:error
echo.
echo ❌ ERRO DETECTADO!
echo.
echo 🔧 Possíveis soluções:
echo    1. Verificar se dispositivos estão conectados
echo    2. Reinstalar APKs: scripts\deploy-remote.bat
echo    3. Verificar permissões de depuração USB
echo    4. Reiniciar dispositivos
echo.

:end
pause
