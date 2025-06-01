@echo off
:: ===================================================================
:: SCRIPT MASTER - TESTE COMPLETO REMOTO VUZIX BLADE
:: Executa todo o processo de preparação e teste
:: ===================================================================

echo.
echo ========================================
echo   TESTE REMOTO COMPLETO - VUZIX BLADE
echo ========================================
echo.

echo 🚀 Este script vai executar:
echo    1. Verificação pré-build
echo    2. Build dos APKs
echo    3. Deploy nos dispositivos
echo    4. Testes automatizados
echo    5. Monitoramento de logs
echo.

set /p CONFIRM=Continuar? (s/N): 
if /i not "%CONFIRM%"=="s" (
    echo ❌ Operação cancelada
    pause
    exit /b 0
)

echo.
echo ========================================
echo   FASE 1: VERIFICAÇÃO PRÉ-BUILD
echo ========================================

call scripts\pre-build-check.bat
if %errorlevel% neq 0 (
    echo ❌ Falhas na verificação pré-build!
    echo 🔧 Corrija os problemas antes de continuar
    pause
    exit /b 1
)

echo.
echo ========================================
echo   FASE 2: BUILD DOS APKs
echo ========================================

call scripts\build-release.bat
if %errorlevel% neq 0 (
    echo ❌ Falha no build!
    pause
    exit /b 1
)

echo.
echo ========================================
echo   FASE 3: DEPLOY NOS DISPOSITIVOS
echo ========================================

call scripts\deploy-remote.bat
if %errorlevel% neq 0 (
    echo ❌ Falha no deploy!
    echo 💡 Verifique se os dispositivos estão conectados
    pause
    exit /b 1
)

echo.
echo ========================================
echo   FASE 4: TESTES AUTOMATIZADOS
echo ========================================

call scripts\test-scenarios.bat

echo.
echo ========================================
echo   FASE 5: MONITORAMENTO
echo ========================================

echo 🔍 Iniciando monitoramento de logs...
echo 💡 Pressione Ctrl+C quando terminar os testes manuais
echo.

call scripts\monitor-logs.bat

echo.
echo ========================================
echo   TESTE COMPLETO FINALIZADO!
echo ========================================
echo.
echo 🎉 Todas as fases foram executadas!
echo.
echo 📋 Próximos passos:
echo    1. Revisar logs gerados
echo    2. Testar funcionalidades manualmente nos dispositivos
echo    3. Documentar problemas encontrados
echo    4. Planejar correções necessárias
echo.

pause
