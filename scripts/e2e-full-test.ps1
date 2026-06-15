# Full E2E smoke test for Banque Agence PFA
$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8081'
$results = @()
$suffix = Get-Date -Format 'HHmmssfff'
$testCin = "FULL$suffix"
$testUser = "z$suffix"

function Record-Test($module, $name, $ok, $detail) {
    $script:results += [pscustomobject]@{ Module = $module; Test = $name; OK = $ok; Detail = $detail }
    $s = if ($ok) { 'PASS' } else { 'FAIL' }
    Write-Output "[$s] [$module] $name - $detail"
}

function New-Session { New-Object Microsoft.PowerShell.Commands.WebRequestSession }

function Get-Csrf([string]$html) {
    if ($html -match 'name="_csrf"\s+value="([^"]+)"') { return $Matches[1] }
    if ($html -match 'value="([^"]+)"\s+name="_csrf"') { return $Matches[1] }
    throw 'CSRF token not found'
}

function Login($session, $user, $pass) {
    $lp = Invoke-WebRequest "$base/login" -WebSession $session -UseBasicParsing
    Invoke-WebRequest "$base/login" -Method POST -WebSession $session -UseBasicParsing -Body @{
        username = $user; password = $pass; _csrf = (Get-Csrf $lp.Content)
    }
}

function PostFrom($session, $getPath, $postPath, $fields) {
    $p = Invoke-WebRequest "$base$getPath" -WebSession $session -UseBasicParsing
    $b = @{}; $fields.Keys | ForEach-Object { $b[$_] = [string]$fields[$_] }; $b['_csrf'] = (Get-Csrf $p.Content)
    Invoke-WebRequest "$base$postPath" -Method POST -WebSession $session -UseBasicParsing -Body $b
}

function Dashboard($r) { [string]$r.BaseResponse.ResponseUri -match '/dashboard$' -and $r.Content -match 'Bienvenue' }
function LoginError($r) { [string]$r.BaseResponse.ResponseUri -match 'login\?error' }
function UserId($session, $username) {
    for ($page = 0; $page -lt 20; $page++) {
        $html = (Invoke-WebRequest "$base/admin/users?page=$page" -WebSession $session -UseBasicParsing).Content
        foreach ($row in [regex]::Matches($html, '(?s)<tr>.*?</tr>')) {
            if ($row.Value -match "<td>\s*$([regex]::Escape($username))\s*</td>" -and $row.Value -match '/admin/users/(\d+)/edit') {
                return [long]$Matches[1]
            }
        }
        if ($html -notmatch '/admin/users\?page=' -or $html -match 'page-item disabled[^>]*>\s*<a class="page-link"[^>]*>Suivant') {
            break
        }
    }
    throw "no id for $username"
}
function ClientId($resp) {
    $uri = [string]$resp.BaseResponse.ResponseUri
    if ($uri -match 'clients/(\d+)') { return [long]$Matches[1] }
    if ($resp.Content -match 'clients/(\d+)') { return [long]$Matches[1] }
    throw 'client id not found'
}
function AccountId($resp) {
    $uri = [string]$resp.BaseResponse.ResponseUri
    if ($uri -match 'accounts/(\d+)') { return [long]$Matches[1] }
    throw 'account id not found'
}
function Balance($html) {
    if ($html -match '<strong[^>]*>([\d\s.,]+)\s*MAD</strong>') {
        return [decimal](($Matches[1] -replace '\s', '') -replace ',', '.')
    }
    throw 'balance not found'
}
function HttpStatus($session, $path) {
    try {
        $r = Invoke-WebRequest "$base$path" -WebSession $session -UseBasicParsing
        return $r.StatusCode
    } catch [System.Net.WebException] {
        return [int]$_.Exception.Response.StatusCode
    }
}

Write-Output '=== FULL E2E TEST ==='
Write-Output ''

# --- AUTH ---
$s = New-Session
Record-Test 'Auth' 'Admin login' (Dashboard (Login $s 'admin' 'admin123')) 'OK'
Record-Test 'Auth' 'Admin can access /admin/users' ((HttpStatus $s '/admin/users') -eq 200) 'HTTP 200'
$s = New-Session
Login $s 'agent' 'agent123' | Out-Null
Record-Test 'Auth' 'Agent login' (Dashboard (Invoke-WebRequest "$base/dashboard" -WebSession $s -UseBasicParsing)) 'OK'
Record-Test 'Auth' 'Agent blocked from admin' ((HttpStatus $s '/admin/users') -eq 403) 'HTTP 403'
$ch = New-Session
Record-Test 'Auth' 'Chef login' (Dashboard (Login $ch 'chef' 'chef123')) 'OK'

# --- CLIENTS ---
$agent = New-Session
Login $agent 'agent' 'agent123' | Out-Null
try {
    $cr = PostFrom $agent '/clients/new' '/clients' @{
        firstName = 'Full'; lastName = 'Test'; cin = $testCin; email = "$testUser@test.local"; phone = '0600000000'
    }
    $clientId = ClientId $cr
    Record-Test 'Clients' 'Create client' ($clientId -gt 0) "id=$clientId"
    $list = Invoke-WebRequest "$base/clients?q=$testCin" -WebSession $agent -UseBasicParsing
    Record-Test 'Clients' 'Search client by CIN' ($list.Content -match $testCin) 'found'
} catch { Record-Test 'Clients' 'Create client' $false $_.Exception.Message; throw }

# --- ACCOUNTS ---
try {
    $a1 = PostFrom $agent "/accounts/client/$clientId/new" "/accounts/client/$clientId" @{ type = 'COURANT' }
    $acc1 = AccountId $a1
    $a2 = PostFrom $agent "/accounts/client/$clientId/new" "/accounts/client/$clientId" @{ type = 'EPARGNE' }
    $acc2 = AccountId $a2
    Record-Test 'Accounts' 'Open 2 accounts' ($acc1 -ne $acc2) "acc1=$acc1 acc2=$acc2"
    $detail = Invoke-WebRequest "$base/accounts/$acc1" -WebSession $agent -UseBasicParsing
    Record-Test 'Accounts' 'Account detail page' ($detail.StatusCode -eq 200 -and $detail.Content -match 'ACC-') 'OK'
} catch { Record-Test 'Accounts' 'Open accounts' $false $_.Exception.Message; throw }

# --- TRANSACTIONS ---
try {
    PostFrom $agent '/operations/deposit' '/operations/deposit' @{ accountId = $acc1; amount = '1000' } | Out-Null
    Record-Test 'Transactions' 'Deposit 1000' ((Balance (Invoke-WebRequest "$base/accounts/$acc1" -WebSession $agent -UseBasicParsing).Content) -eq 1000) 'balance=1000'

    PostFrom $agent '/operations/withdraw' '/operations/withdraw' @{ accountId = $acc1; amount = '300' } | Out-Null
    Record-Test 'Transactions' 'Withdraw 300 -> 700' ((Balance (Invoke-WebRequest "$base/accounts/$acc1" -WebSession $agent -UseBasicParsing).Content) -eq 700) 'OK'

    $wFail = PostFrom $agent '/operations/withdraw' '/operations/withdraw' @{ accountId = $acc1; amount = '800' }
    Record-Test 'Transactions' 'Withdraw 800 refused' ($wFail.Content -match 'Solde insuffisant') 'OK'

    PostFrom $agent '/operations/transfer' '/operations/transfer' @{
        sourceAccountId = $acc1; destinationAccountId = $acc2; amount = '200'
    } | Out-Null
    $b1 = Balance (Invoke-WebRequest "$base/accounts/$acc1" -WebSession $agent -UseBasicParsing).Content
    $b2 = Balance (Invoke-WebRequest "$base/accounts/$acc2" -WebSession $agent -UseBasicParsing).Content
    Record-Test 'Transactions' 'Transfer 200 -> 500/200' (($b1 -eq 500) -and ($b2 -eq 200)) "src=$b1 dst=$b2"

    PostFrom $agent "/accounts/$acc1" "/accounts/$acc1/block" @{} | Out-Null
    $tFail = PostFrom $agent '/operations/transfer' '/operations/transfer' @{
        sourceAccountId = $acc1; destinationAccountId = $acc2; amount = '50'
    }
    Record-Test 'Transactions' 'Transfer from blocked refused' ($tFail.Content -match 'actif') 'OK'
    PostFrom $agent "/accounts/$acc1" "/accounts/$acc1/unblock" @{} | Out-Null

    $accDetail = Invoke-WebRequest "$base/accounts/$acc1" -WebSession $agent -UseBasicParsing
    $accNum = if ($accDetail.Content -match '(ACC-\d+)') { $Matches[1] } else { '' }
    $hist = Invoke-WebRequest "$base/transactions?type=DEPOT&accountNumber=$accNum" -WebSession $agent -UseBasicParsing
    $tbody = if ($hist.Content -match '(?s)<tbody>(.*?)</tbody>') { $Matches[1] } else { '' }
    $rowCount = if ($tbody) { ([regex]::Matches($tbody, '<tr>')).Count } else { 0 }
    Record-Test 'Transactions' 'History DEPOT filter' (
        ($hist.StatusCode -eq 200) -and ($rowCount -ge 1) -and -not ($tbody -match '<td>Retrait</td>')
    ) "acc=$accNum rows=$rowCount"

    $overflowPage = PostFrom $agent '/operations/deposit' '/operations/deposit' @{ accountId = $acc1; amount = '1000000000000000' }
    Record-Test 'Transactions' 'Huge amount validation (no 500)' (
        ($overflowPage.StatusCode -eq 200) -and ($overflowPage.Content -match 'lev|Montant|deposit')
    ) 'form error not server error'
} catch { Record-Test 'Transactions' 'Transaction flow' $false $_.Exception.Message }

# --- USERS (ADMIN) ---
$admin = New-Session
Login $admin 'admin' 'admin123' | Out-Null
try {
    Record-Test 'Users' 'Admin user list' ((Invoke-WebRequest "$base/admin/users" -WebSession $admin -UseBasicParsing).StatusCode -eq 200) 'OK'
    PostFrom $admin '/admin/users/new' '/admin/users' @{
        username = $testUser; password = 'secret123'; fullName = 'E2E User'; email = "$testUser@t.local"; role = 'AGENT'
    } | Out-Null
    $uid = UserId $admin $testUser
    Record-Test 'Users' 'Admin creates user' ($uid -gt 0) "id=$uid"

    $ns = New-Session
    Record-Test 'Users' 'New user login' (Dashboard (Login $ns $testUser 'secret123')) 'OK'

    PostFrom $admin '/admin/users' "/admin/users/$uid/disable" @{} | Out-Null
    $ns2 = New-Session
    Record-Test 'Users' 'Disabled user blocked' (LoginError (Login $ns2 $testUser 'secret123')) 'login?error'

    $disableAdmin = PostFrom $admin '/admin/users' '/admin/users/1/disable' @{}
    Record-Test 'Users' 'Last admin protected' ($disableAdmin.Content -match 'administrateur') 'error shown'
} catch { Record-Test 'Users' 'Admin user mgmt' $false $_.Exception.Message }

# --- PAGES SMOKE ---
$pages = @(
    @{ M = 'UI'; N = 'Dashboard'; P = '/dashboard' },
    @{ M = 'UI'; N = 'Clients list'; P = '/clients' },
    @{ M = 'UI'; N = 'Accounts list'; P = '/accounts' },
    @{ M = 'UI'; N = 'Operations'; P = '/operations' },
    @{ M = 'UI'; N = 'Transactions'; P = '/transactions' }
)
foreach ($pg in $pages) {
    Record-Test $pg.M $pg.N ((HttpStatus $agent $pg.P) -eq 200) 'HTTP 200'
}

Write-Output ''
Write-Output '=== SUMMARY ==='
$passed = @($results | Where-Object { $_.OK }).Count
$failed = @($results | Where-Object { -not $_.OK })
Write-Output "Passed: $passed / $($results.Count)"
if ($failed.Count -gt 0) {
    Write-Output 'Failures:'
    $failed | Format-Table -AutoSize
    exit 1
}
$results | Format-Table -AutoSize
exit 0
