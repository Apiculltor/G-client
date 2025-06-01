@echo off
:: ===================================================================
:: 🚀 INICIAR TESTE REMOTO - VUZIX BLADE + SAMSUNG S24 ULTRA
:: Script principal para execução completa do teste
:: ===================================================================

title Teste Remoto - Vuzix Blade + Samsung S24 Ultra

echo.
echo ╔════════════════════════════════════════════════════════════════╗
echo ║                  TESTE REMOTO VUZIX BLADE                     ║
echo ║              + SAMSUNG GALAXY S24 ULTRA                       ║
echo ╚════════════════════════════════════════════════════════════════╝
echo.

echo 🎯 PROJETO: Integração de óculos inteligentes com processamento IA
echo 📱 DISPOSITIVOS: Vuzix Blade (API 22) + Samsung S24 Ultra
echo ⚡ OBJETIVO: Teste funcional da comunicação e captura A/V
echo.

echo ========================================
echo   OPÇÕES DE TESTE DISPONÍVEIS
echo ========================================
echo.
echo 1. 🚀 TESTE COMPLETO AUTOMÁTICO (Recomendado)
echo    - Verificação, build, deploy e teste
echo    - Tempo: ~15 minutos
echo.
echo 2. 🔧 APENAS PREPARAÇÃO (Para TeamViewer)
echo    - Build dos APKs + verificações
echo    - Tempo: ~5 minutos
echo.
echo 3. 📱 APENAS DEPLOY (Hardware já conectado)
echo    - Deploy + testes nos dispositivos
echo    - Tempo: ~8 minutos
echo.
echo 4. 📊 APENAS MONITORAMENTO (Apps já instalados)
echo    - Logs em tempo real
echo    - Tempo: Contínuo
echo.
echo 5. 📖 ABRIR DOCUMENTAÇÃO
echo    - Guias e manuais completos
echo.

set /p OPCAO=Escolha uma opção (1-5): 

echo.

if "%OPCAO%"=="1" goto :teste_completo
if "%OPCAO%"=="2" goto :preparacao
if "%OPCAO%"=="3" goto :deploy
if "%OPCAO%"=="4" goto :monitoramento
if "%OPCAO%"=="5" goto :documentacao

echo ❌ Opção inválida!
pause
exit /b 1

:teste_completo
echo ========================================
echo   🚀 TESTE COMPLETO AUTOMÁTICO
echo ========================================
echo.
echo Este processo vai executar:
echo ✅ Verificação pré-build
echo ✅ Build dos APKs otimizados
echo ✅ Deploy nos dispositivos
echo ✅ Testes automatizados
echo ✅ Monitoramento de logs
echo.
set /p CONFIRM=Continuar? (s/N): 
if /i not "%CONFIRM%"=="s" goto :end
call scripts\teste-completo.bat
goto :end

:preparacao
echo ========================================
echo   🔧 PREPARAÇÃO PARA TEAMVIEWER
echo ========================================
echo.
echo Executando verificações e build...
call scripts\pre-build-check.bat
if %errorlevel% equ 0 call scripts\build-release.bat
echo.
echo ✅ Preparação concluída!
echo 💡 Agora você pode conectar via TeamViewer e executar:
echo    scripts\deploy-remote.bat
goto :end

:deploy
echo ========================================
echo   📱 DEPLOY E TESTE
echo ========================================
echo.
echo Verificando dispositivos conectados...
adb devices
echo.
set /p CONFIRM=Dispositivos OK? Continuar com deploy? (s/N): 
if /i not "%CONFIRM%"=="s" goto :end
call scripts\deploy-remote.bat
if %errorlevel% equ 0 call scripts\test-scenarios.bat
goto :end

:monitoramento
echo ========================================
echo   📊 MONITORAMENTO DE LOGS
echo ========================================
echo.
call scripts\monitor-logs.bat
goto :end

:documentacao
echo ========================================
echo   📖 DOCUMENTAÇÃO DISPONÍVEL
echo ========================================
echo.
echo 📁 Guias principais:
echo    - README_TESTE_REMOTO.md (Visão geral)
echo    - scripts\teamviewer-guide.md (TeamViewer)
echo    - TESTE_REMOTO_COMPLETO.md (Documentação técnica)
echo.
echo 📁 Scripts disponíveis:
echo    - scripts\deploy-remote.bat
echo    - scripts\test-scenarios.bat
echo    - scripts\monitor-logs.bat
echo    - scripts\build-release.bat
echo.
echo 🌐 Abrindo README principal...
start README_TESTE_REMOTO.md
goto :end

:end
echo.
echo ========================================
echo   PRÓXIMOS PASSOS
echo ========================================
echo.
echo 📋 Após o teste, documente:
echo    ✅ Funcionalidades que funcionaram
echo    ❌ Problemas encontrados
echo    🔧 Melhorias necessárias
echo    📅 Próxima sessão de testes
echo.
echo 📞 Mantenha comunicação com seu sócio via:
echo    - WhatsApp/Telegram para feedback rápido
echo    - Screenshots da interface do Blade
echo    - Descrição detalhada de problemas
echo.
echo 🎯 Objetivo alcançado se:
echo    ✅ Apps instalam e funcionam
echo    ✅ Interface é navegável
echo    ✅ Comunicação básica OK
echo    ✅ Captura A/V possível
echo.

pause
