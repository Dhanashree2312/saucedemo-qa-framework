
$ErrorActionPreference = "Continue"

# Always run Maven from the project root.
Set-Location $PSScriptRoot
# Select the local AWS credentials profile
$env:AWS_PROFILE = "saucedemo"

Write-Host "Using AWS profile: $env:AWS_PROFILE"
$executionId = Get-Date -Format "yyyyMMdd-HHmmss-fff"

Write-Host ""
Write-Host "========================================"
Write-Host "Starting Complete QA Automation Suite"
Write-Host "Execution ID: $executionId"
Write-Host "========================================"

# Step 1: Run Web + API + Mobile tests.
mvn clean test

$testExitCode = $LASTEXITCODE

Write-Host ""
Write-Host "Maven test exit code: $testExitCode"

# Step 2: Generate the Allure HTML report.
# This is attempted even when tests fail.
Write-Host ""
Write-Host "Generating Allure HTML report..."

mvn -q allure:report

$allureExitCode = $LASTEXITCODE

if ($allureExitCode -ne 0) {
    Write-Warning "Allure report generation failed. Raw results will still be uploaded."
}

# Step 3: Compile test classes and run the S3 uploader.
Write-Host ""
Write-Host "Uploading execution artifacts to S3..."

mvn -q -DskipTests test-compile exec:java `
    "-Dexec.mainClass=com.qa.tests.reporting.CloudArtifactUploader" `
    "-Dexec.classpathScope=test" `
    "-Dexec.args=$executionId $testExitCode"

$uploadExitCode = $LASTEXITCODE

# Step 4: Report final status.
Write-Host ""
Write-Host "========================================"
Write-Host "QA EXECUTION SUMMARY"
Write-Host "========================================"

Write-Host "Execution ID : $executionId"
Write-Host "Test exit code: $testExitCode"
Write-Host "Allure exit code: $allureExitCode"
Write-Host "Upload exit code: $uploadExitCode"

if ($testExitCode -ne 0) {
    Write-Host "Overall result: TESTS FAILED"
    exit $testExitCode
}

if ($allureExitCode -ne 0 -or $uploadExitCode -ne 0) {
    Write-Host "Overall result: TESTS PASSED, REPORTING/UPLOAD ISSUE"
    exit 1
}

Write-Host "Overall result: ALL STEPS PASSED"
exit 0
