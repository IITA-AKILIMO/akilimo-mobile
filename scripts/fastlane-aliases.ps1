# IITA Internal Use
#
# Short PowerShell commands for the AKILIMO fastlane lanes. Load them once per session from the repo:
#
#   . .\scripts\fastlane-aliases.ps1
#
# or run `fl-install` once to have them in every new PowerShell window.
# Run `fl-help` for the list. Details: docs/PLAY_STORE_SCREENSHOTS.md

$script:AkilimoRoot = Split-Path -Parent $PSScriptRoot

# Runs `bundle exec fastlane <arguments>` from the repo root, whatever the current folder is.
function Invoke-AkilimoFastlane {
    param([Parameter(ValueFromRemainingArguments = $true)][string[]]$FastlaneArgs)
    Push-Location $script:AkilimoRoot
    # fastlane warns unless the locale is UTF-8; set it for this call only and restore afterwards
    $saved = @{ LANG = $env:LANG; LC_ALL = $env:LC_ALL }
    $env:LANG = "en_US.UTF-8"
    $env:LC_ALL = "en_US.UTF-8"
    try {
        & bundle exec fastlane @FastlaneArgs
        if ($LASTEXITCODE -ne 0) { Write-Error "fastlane exited with code $LASTEXITCODE" }
    }
    finally {
        $env:LANG = $saved.LANG
        $env:LC_ALL = $saved.LC_ALL
        Pop-Location
    }
}

function fl-shots {
    <# Capture screenshots: all languages (en-GB, sw, rw) in turn, or one with -Locale sw. #>
    param([string]$Locale)
    $locales = if ($Locale) { @($Locale) } else { @("en-GB", "sw", "rw") }
    foreach ($tag in $locales) { Invoke-AkilimoFastlane screenshots "locale:$tag" }
}

function fl-shots-en { Invoke-AkilimoFastlane screenshots "locale:en-GB" }
function fl-shots-sw { Invoke-AkilimoFastlane screenshots "locale:sw" }
function fl-shots-rw { Invoke-AkilimoFastlane screenshots "locale:rw" }

function fl-upload {
    <# Upload the store_shots selection (max 8 per language) to the Play listing; -DryRun only lists them. #>
    param([switch]$DryRun)
    if ($DryRun) { Invoke-AkilimoFastlane upload_screenshots "dry_run:true" } else { Invoke-AkilimoFastlane upload_screenshots }
}

function fl-notes {
    <# Correct the release notes of a published version: fl-notes -Code 123 [-Track beta] [-DryRun] #>
    param(
        # Play's version CODE, a whole number such as 345, not the version name (33.0.3)
        [Parameter(Mandatory = $true)]
        [ValidatePattern('^\d+$', ErrorMessage = "-Code must be Play's version code, a whole number such as 345, not the version name like 33.0.3. Find it in Play Console under Release > App bundle explorer.")]
        [string]$Code,
        [string]$Track,
        [switch]$DryRun
    )
    $lane = @("update_release_notes", "version_code:$Code")
    if ($Track) { $lane += "track:$Track" }
    if ($DryRun) { $lane += "dry_run:true" }
    Invoke-AkilimoFastlane @lane
}

$script:ProfileBegin = "# >>> akilimo fastlane commands >>>"
$script:ProfileEnd = "# <<< akilimo fastlane commands <<<"

function fl-install {
    <# Load these commands in every new PowerShell window by adding a block to your profile. Safe to repeat. #>
    param([string]$ProfilePath = $PROFILE.CurrentUserAllHosts)
    $script = Join-Path (Join-Path $script:AkilimoRoot "scripts") "fastlane-aliases.ps1"
    $block = @($script:ProfileBegin, ". `"$script`"", $script:ProfileEnd) -join [Environment]::NewLine

    $dir = Split-Path -Parent $ProfilePath
    if (-not (Test-Path $dir)) { New-Item -ItemType Directory -Path $dir | Out-Null }
    $current = if (Test-Path $ProfilePath) { Get-Content -Raw $ProfilePath } else { "" }
    if ($null -eq $current) { $current = "" }

    if ($current.Contains($script:ProfileBegin)) {
        Write-Host "Already installed in $ProfilePath (nothing changed)."
        return
    }
    $separator = if ($current -and -not $current.EndsWith("`n")) { [Environment]::NewLine } else { "" }
    Set-Content -Path $ProfilePath -Value ($current + $separator + $block + [Environment]::NewLine) -NoNewline
    Write-Host "Added the fl- commands to $ProfilePath. Open a new PowerShell window to use them."
}

function fl-uninstall {
    <# Remove the block that fl-install added to your profile. #>
    param([string]$ProfilePath = $PROFILE.CurrentUserAllHosts)
    if (-not (Test-Path $ProfilePath)) { Write-Host "No profile at $ProfilePath."; return }
    $current = Get-Content -Raw $ProfilePath
    $pattern = "(?s)" + [regex]::Escape($script:ProfileBegin) + ".*?" + [regex]::Escape($script:ProfileEnd) + "(\r?\n)?"
    if ($current -notmatch $pattern) { Write-Host "Nothing to remove in $ProfilePath."; return }
    Set-Content -Path $ProfilePath -Value ([regex]::Replace($current, $pattern, "")) -NoNewline
    Write-Host "Removed the fl- commands from $ProfilePath."
}

function fl-help {
    @"
AKILIMO Play Store commands (PowerShell)

  Screenshots (phone or emulator connected, adb on PATH)
    fl-shots                 capture en-GB, sw and rw in turn
    fl-shots -Locale sw      capture one language
    fl-shots-en | fl-shots-sw | fl-shots-rw
    output: fastlane\screenshots\<locale>\NN_name.png

  Upload (key: repo-root service-account JSON, or SUPPLY_JSON_KEY / SUPPLY_JSON_KEY_DATA)
    fl-upload                send the 8 store_shots per language to Play
    fl-upload -DryRun        list what would be sent, upload nothing

  Release notes (reads release\distribution\whatsnew\whatsnew-<locale>, max 500 characters)
    fl-notes -Code 123                     replace the notes of version code 123 (production)
    fl-notes -Code 123 -Track beta
    fl-notes -Code 123 -DryRun             check the files only, upload nothing

  Other
    fl-help                  this list
    fl-install               load these commands in every new window (adds a block to your profile)
    fl-uninstall             remove that block again

  Full guide: docs\PLAY_STORE_SCREENSHOTS.md
"@ | Write-Host
}
