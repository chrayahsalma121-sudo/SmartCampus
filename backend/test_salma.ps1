$base = "http://localhost:8080"
$p = 0
$f = 0

function Req($method, $url, $hdrs, $body) {
    $wc = New-Object System.Net.WebClient
    foreach ($k in $hdrs.Keys) { $wc.Headers.Add($k, $hdrs[$k]) }
    $wc.Headers["Content-Type"] = "application/json"
    try {
        $payload = if ($body) { $body } else { "{}" }
        $c = if ($method -eq "GET") { $wc.DownloadString($url) } else { $wc.UploadString($url, "POST", $payload) }
        return @{ s = 200; c = $c }
    } catch [System.Net.WebException] {
        $resp = $_.Exception.Response
        $code = [int]$resp.StatusCode
        $c = [System.IO.StreamReader]::new($resp.GetResponseStream()).ReadToEnd()
        return @{ s = $code; c = $c }
    }
}

function T($label, $r, $expS, $expStr) {
    $cOk = ($expS -in @(200, 201) -and $r.s -in @(200, 201)) -or ($expS -ge 400 -and $r.s -eq $expS)
    $sOk = if ($expStr) { $r.c -match [regex]::Escape($expStr) } else { $true }
    if ($cOk -and $sOk) {
        Write-Host "  PASS [$label]"
        $script:p++
    } else {
        Write-Host "  FAIL [$label] HTTP=$($r.s) | $($r.c)"
        $script:f++
    }
}

# Get tokens
$r = Req POST "$base/api/auth/login" @{} '{"email":"student@example.com","password":"1234"}'
$sT = ($r.c | ConvertFrom-Json).data.accessToken
$r = Req POST "$base/api/auth/login" @{} '{"email":"admin@example.com","password":"1234"}'
$aT = ($r.c | ConvertFrom-Json).data.accessToken
$r = Req POST "$base/api/auth/login" @{} '{"email":"librarian@example.com","password":"1234"}'
$lT = ($r.c | ConvertFrom-Json).data.accessToken
$sH = @{ Authorization = "Bearer $sT" }
$aH = @{ Authorization = "Bearer $aT" }
$lH = @{ Authorization = "Bearer $lT" }

Write-Host ""
Write-Host "############################################################"
Write-Host "#        SALMA'S ENDPOINTS - EXHAUSTIVE TEST SUITE         #"
Write-Host "############################################################"

# SECTION 1: LIST ROOMS
Write-Host "`n====== SECTION 1: GET /api/rooms ======"
$r = Req GET "$base/api/rooms" $sH $null
T "1.1 Returns array"             $r 200 "roomId"
T "1.2 Contains Room A101"        $r 200 "Room A101"
T "1.3 Contains Room B204"        $r 200 "Room B204"
T "1.4 Contains Lab C301"         $r 200 "Lab C301"
T "1.5 Contains Conference"       $r 200 "Conference"
T "1.6 Contains capacity"         $r 200 "capacity"
T "1.7 Contains available"        $r 200 "available"
T "1.8 Admin can list"            (Req GET "$base/api/rooms" $aH $null) 200 "roomId"
T "1.9 No-auth can list (public)" (Req GET "$base/api/rooms" @{} $null) 200 "roomId"

# SECTION 2: RESERVE ROOM
Write-Host "`n====== SECTION 2: POST /api/rooms/reserve ======"
$d1 = (Get-Date).AddDays(5).ToString("yyyy-MM-dd")
$d2 = (Get-Date).AddDays(6).ToString("yyyy-MM-dd")
$d3 = (Get-Date).AddDays(7).ToString("yyyy-MM-dd")

$r = Req POST "$base/api/rooms/reserve" $sH "{`"roomId`":1,`"reservationDate`":`"$d1`",`"startTime`":`"09:00`",`"endTime`":`"11:00`"}"
T "2.1 Reserve room 1"           $r 201 "reservationId"
T "2.2 Status CONFIRMED"         $r 201 "CONFIRMED"
T "2.3 Has studentId"            $r 201 "studentId"
T "2.4 Has roomId"               $r 201 "roomId"
T "2.5 Has reservationDate"      $r 201 "reservationDate"
T "2.6 Has startTime"            $r 201 "startTime"
T "2.7 Has endTime"              $r 201 "endTime"
$res1Id = ($r.c | ConvertFrom-Json).data.reservationId

$r = Req POST "$base/api/rooms/reserve" $sH "{`"roomId`":2,`"reservationDate`":`"$d1`",`"startTime`":`"14:00`",`"endTime`":`"16:00`"}"
T "2.8 Reserve different room/slot"  $r 201 "CONFIRMED"
$res2Id = ($r.c | ConvertFrom-Json).data.reservationId

T "2.9  No token -> 401"          (Req POST "$base/api/rooms/reserve" @{} "{`"roomId`":3,`"reservationDate`":`"$d2`",`"startTime`":`"09:00`",`"endTime`":`"11:00`"}") 401 "Unauthorized"
T "2.10 Admin reserve -> 400"     (Req POST "$base/api/rooms/reserve" $aH "{`"roomId`":3,`"reservationDate`":`"$d2`",`"startTime`":`"09:00`",`"endTime`":`"11:00`"}") 400 "Only students can reserve rooms"
T "2.11 Librarian reserve -> 400" (Req POST "$base/api/rooms/reserve" $lH "{`"roomId`":3,`"reservationDate`":`"$d2`",`"startTime`":`"09:00`",`"endTime`":`"11:00`"}") 400 "Only students can reserve rooms"
T "2.12 Room not found -> 400"    (Req POST "$base/api/rooms/reserve" $sH "{`"roomId`":9999,`"reservationDate`":`"$d2`",`"startTime`":`"09:00`",`"endTime`":`"11:00`"}") 400 "Room not found"
T "2.13 Room unavailable -> 400"  (Req POST "$base/api/rooms/reserve" $sH "{`"roomId`":4,`"reservationDate`":`"$d2`",`"startTime`":`"09:00`",`"endTime`":`"11:00`"}") 400 "not available for reservation"
T "2.14 Equal start/end -> 400"   (Req POST "$base/api/rooms/reserve" $sH "{`"roomId`":3,`"reservationDate`":`"$d2`",`"startTime`":`"10:00`",`"endTime`":`"10:00`"}") 400 "End time must be after start time"
T "2.15 End before start -> 400"  (Req POST "$base/api/rooms/reserve" $sH "{`"roomId`":3,`"reservationDate`":`"$d2`",`"startTime`":`"14:00`",`"endTime`":`"09:00`"}") 400 "End time must be after start time"

$r = Req POST "$base/api/rooms/reserve" $sH "{`"roomId`":3,`"reservationDate`":`"$d3`",`"startTime`":`"10:00`",`"endTime`":`"12:00`"}"
T "2.16 Reserve room 3 (setup)"   $r 201 "CONFIRMED"
T "2.17 Same slot conflict -> 400"       (Req POST "$base/api/rooms/reserve" $sH "{`"roomId`":3,`"reservationDate`":`"$d3`",`"startTime`":`"10:00`",`"endTime`":`"12:00`"}") 400 "already reserved for the selected time slot"
T "2.18 Overlapping slot -> 400"         (Req POST "$base/api/rooms/reserve" $sH "{`"roomId`":3,`"reservationDate`":`"$d3`",`"startTime`":`"11:00`",`"endTime`":`"13:00`"}") 400 "already reserved"

$r = Req POST "$base/api/rooms/reserve" $sH "{`"roomId`":1,`"reservationDate`":`"$d2`",`"startTime`":`"09:00`",`"endTime`":`"11:00`"}"
T "2.19 Reserve room 1 on d2"    $r 201 "CONFIRMED"
T "2.20 Student double-book -> 400" (Req POST "$base/api/rooms/reserve" $sH "{`"roomId`":2,`"reservationDate`":`"$d2`",`"startTime`":`"09:00`",`"endTime`":`"11:00`"}") 400 "already have a reservation during this time slot"

# SECTION 3: MY RESERVATIONS
Write-Host "`n====== SECTION 3: GET /api/rooms/my-reservations ======"
$r = Req GET "$base/api/rooms/my-reservations" $sH $null
T "3.1 Returns array"              $r 200 "reservationId"
T "3.2 Contains CONFIRMED"         $r 200 "CONFIRMED"
T "3.3 Has reservationDate"        $r 200 "reservationDate"
T "3.4 Admin -> 400"               (Req GET "$base/api/rooms/my-reservations" $aH $null) 400 "Only students can view their reservations"
T "3.5 Librarian -> 400"           (Req GET "$base/api/rooms/my-reservations" $lH $null) 400 "Only students can view their reservations"
T "3.6 No token -> 401"            (Req GET "$base/api/rooms/my-reservations" @{} $null) 401 "Unauthorized"

# SECTION 4: CANCEL RESERVATION
Write-Host "`n====== SECTION 4: POST /api/rooms/cancel-reservation ======"
T "4.1 Cancel reservation"          (Req POST "$base/api/rooms/cancel-reservation" $sH "{`"reservationId`":$res1Id}") 200 "cancelled successfully"
T "4.2 Already-cancelled -> 400"    (Req POST "$base/api/rooms/cancel-reservation" $sH "{`"reservationId`":$res1Id}") 400 "already cancelled"
T "4.3 Non-existent -> 400"         (Req POST "$base/api/rooms/cancel-reservation" $sH '{"reservationId":9999}') 400 "Reservation not found"
T "4.4 Admin cancel -> 400"         (Req POST "$base/api/rooms/cancel-reservation" $aH "{`"reservationId`":$res2Id}") 400 "Only students can cancel"
T "4.5 No token -> 401"             (Req POST "$base/api/rooms/cancel-reservation" @{} "{`"reservationId`":$res2Id}") 401 "Unauthorized"

# SECTION 5: ADMIN ADD ROOM
Write-Host "`n====== SECTION 5: POST /api/admin/rooms ======"
$r = Req POST "$base/api/admin/rooms" $aH '{"name":"Lab Test","capacity":20,"available":true}'
T "5.1 Add room"               $r 201 "Lab Test"
T "5.2 Has roomId"             $r 201 "roomId"
T "5.3 Has capacity"           $r 201 "capacity"
$newRoomId = ($r.c | ConvertFrom-Json).data.roomId

T "5.4 Add unavailable room"   (Req POST "$base/api/admin/rooms" $aH '{"name":"Unavail","capacity":10,"available":false}') 201 "Unavail"
T "5.5 Student add -> 400"     (Req POST "$base/api/admin/rooms" $sH '{"name":"X","capacity":5,"available":true}') 400 "Only admins can add rooms"
T "5.6 Librarian add -> 400"   (Req POST "$base/api/admin/rooms" $lH '{"name":"X","capacity":5,"available":true}') 400 "Only admins can add rooms"
T "5.7 Empty name -> 400"      (Req POST "$base/api/admin/rooms" $aH '{"name":"","capacity":10,"available":true}') 400 "Room name is required"
T "5.8 Zero capacity -> 400"   (Req POST "$base/api/admin/rooms" $aH '{"name":"X","capacity":0,"available":true}') 400 "Capacity must be greater than 0"
T "5.9 Negative cap -> 400"    (Req POST "$base/api/admin/rooms" $aH '{"name":"X","capacity":-5,"available":true}') 400 "Capacity must be greater than 0"
T "5.10 No token -> 401"       (Req POST "$base/api/admin/rooms" @{} '{"name":"X","capacity":5,"available":true}') 401 "Unauthorized"

# SECTION 6: UPDATE AVAILABILITY
Write-Host "`n====== SECTION 6: POST /api/admin/rooms/update-availability ======"
T "6.1 Set unavailable"         (Req POST "$base/api/admin/rooms/update-availability" $aH "{`"roomId`":$newRoomId,`"available`":false}") 200 "available"
T "6.2 Set available again"     (Req POST "$base/api/admin/rooms/update-availability" $aH "{`"roomId`":$newRoomId,`"available`":true}") 200 "available"
T "6.3 Non-existent -> 400"     (Req POST "$base/api/admin/rooms/update-availability" $aH '{"roomId":9999,"available":false}') 400 "Room not found"
T "6.4 Student update -> 400"   (Req POST "$base/api/admin/rooms/update-availability" $sH "{`"roomId`":$newRoomId,`"available`":false}") 400 "Only admins can update room availability"
T "6.5 No token -> 401"         (Req POST "$base/api/admin/rooms/update-availability" @{} "{`"roomId`":$newRoomId,`"available`":false}") 401 "Unauthorized"

# SECTION 7: SUBMIT ADMINISTRATIVE REQUEST
Write-Host "`n====== SECTION 7: POST /api/requests ======"
$r = Req POST "$base/api/requests" $sH '{"type":"TRANSCRIPT","description":"Need for visa."}'
T "7.1 Submit TRANSCRIPT"        $r 201 "requestId"
T "7.2 Status PENDING"           $r 201 "PENDING"
T "7.3 Has studentId"            $r 201 "studentId"
T "7.4 Has type TRANSCRIPT"      $r 201 "TRANSCRIPT"
T "7.5 Has description"          $r 201 "visa"
T "7.6 Has submissionDate"       $r 201 "submissionDate"
T "7.7 refusalReason is null"    $r 201 "null"
$rqId1 = ($r.c | ConvertFrom-Json).data.requestId

$r = Req POST "$base/api/requests" $sH '{"type":"SCHOOL_CERTIFICATE","description":"For internship."}'
T "7.8 Submit SCHOOL_CERTIFICATE" $r 201 "PENDING"
$rqId2 = ($r.c | ConvertFrom-Json).data.requestId

$r = Req POST "$base/api/requests" $sH '{"type":"ATTENDANCE_CERTIFICATE","description":"For job."}'
T "7.9 Submit ATTENDANCE_CERTIFICATE" $r 201 "PENDING"
$rqId3 = ($r.c | ConvertFrom-Json).data.requestId

$r = Req POST "$base/api/requests" $sH '{"type":"OTHER","description":"Custom."}'
T "7.10 Submit OTHER"            $r 201 "PENDING"
$rqId4 = ($r.c | ConvertFrom-Json).data.requestId

T "7.11 Lowercase type ok"       (Req POST "$base/api/requests" $sH '{"type":"transcript","description":"lowercase ok"}') 201 "TRANSCRIPT"
T "7.12 Admin submit -> 400"     (Req POST "$base/api/requests" $aH '{"type":"TRANSCRIPT","description":"x"}') 400 "Only students can submit"
T "7.13 Librarian submit -> 400" (Req POST "$base/api/requests" $lH '{"type":"TRANSCRIPT","description":"x"}') 400 "Only students can submit"
T "7.14 Invalid type -> 400"     (Req POST "$base/api/requests" $sH '{"type":"INVALID","description":"x"}') 400 "Invalid request type"
T "7.15 Missing type -> 400"     (Req POST "$base/api/requests" $sH '{"description":"no type"}') 400 "required"
T "7.16 No token -> 401"         (Req POST "$base/api/requests" @{} '{"type":"TRANSCRIPT","description":"x"}') 401 "Unauthorized"

# SECTION 8: MY REQUESTS
Write-Host "`n====== SECTION 8: GET /api/requests/my-requests ======"
$r = Req GET "$base/api/requests/my-requests" $sH $null
T "8.1 Returns array"            $r 200 "requestId"
T "8.2 Contains PENDING"         $r 200 "PENDING"
T "8.3 Contains TRANSCRIPT"      $r 200 "TRANSCRIPT"
T "8.4 Admin -> 400"             (Req GET "$base/api/requests/my-requests" $aH $null) 400 "Only students can view their requests"
T "8.5 Librarian -> 400"         (Req GET "$base/api/requests/my-requests" $lH $null) 400 "Only students can view their requests"
T "8.6 No token -> 401"          (Req GET "$base/api/requests/my-requests" @{} $null) 401 "Unauthorized"

# SECTION 9: ADMIN LIST ALL
Write-Host "`n====== SECTION 9: GET /api/admin/requests ======"
$r = Req GET "$base/api/admin/requests" $aH $null
T "9.1 Admin list all"           $r 200 "requestId"
T "9.2 Has studentId"            $r 200 "studentId"
T "9.3 Student -> 400"           (Req GET "$base/api/admin/requests" $sH $null) 400 "Only admins can view all requests"
T "9.4 Librarian -> 400"         (Req GET "$base/api/admin/requests" $lH $null) 400 "Only admins can view all requests"
T "9.5 No token -> 401"          (Req GET "$base/api/admin/requests" @{} $null) 401 "Unauthorized"

# SECTION 10: APPROVE
Write-Host "`n====== SECTION 10: POST /api/admin/requests/approve ======"
$r = Req POST "$base/api/admin/requests/approve" $aH "{`"requestId`":$rqId1}"
T "10.1 Approve TRANSCRIPT"      $r 200 "APPROVED"
T "10.2 Has requestId"           $r 200 "requestId"
T "10.3 refusalReason null"      $r 200 "null"
T "10.4 Re-approve -> 400"       (Req POST "$base/api/admin/requests/approve" $aH "{`"requestId`":$rqId1}") 400 "Only pending requests can be approved"
T "10.5 Non-existent -> 400"     (Req POST "$base/api/admin/requests/approve" $aH '{"requestId":9999}') 400 "Request not found"
T "10.6 Student approve -> 400"  (Req POST "$base/api/admin/requests/approve" $sH "{`"requestId`":$rqId2}") 400 "Only admins can approve"
T "10.7 Librarian approve -> 400" (Req POST "$base/api/admin/requests/approve" $lH "{`"requestId`":$rqId2}") 400 "Only admins can approve"
T "10.8 No token -> 401"         (Req POST "$base/api/admin/requests/approve" @{} "{`"requestId`":$rqId2}") 401 "Unauthorized"

# SECTION 11: REJECT
Write-Host "`n====== SECTION 11: POST /api/admin/requests/reject ======"
$r = Req POST "$base/api/admin/requests/reject" $aH "{`"requestId`":$rqId2,`"refusalReason`":`"Missing documents.`"}"
T "11.1 Reject request"              $r 200 "REJECTED"
T "11.2 refusalReason is set"        $r 200 "Missing documents"
T "11.3 Has requestId"               $r 200 "requestId"
T "11.4 Re-reject -> 400"            (Req POST "$base/api/admin/requests/reject" $aH "{`"requestId`":$rqId2,`"refusalReason`":`"again`"}") 400 "Only pending requests can be rejected"

$r = Req POST "$base/api/admin/requests/approve" $aH "{`"requestId`":$rqId3}"
T "11.5 Reject approved -> 400"      (Req POST "$base/api/admin/requests/reject" $aH "{`"requestId`":$rqId3,`"refusalReason`":`"Changed mind`"}") 400 "Only pending requests can be rejected"
T "11.6 No reason -> 400"            (Req POST "$base/api/admin/requests/reject" $aH "{`"requestId`":$rqId4}") 400 "refusal reason is required"
T "11.7 Blank reason -> 400"         (Req POST "$base/api/admin/requests/reject" $aH "{`"requestId`":$rqId4,`"refusalReason`":`"  `"}") 400 "refusal reason is required"
T "11.8 Non-existent -> 400"         (Req POST "$base/api/admin/requests/reject" $aH '{"requestId":9999,"refusalReason":"x"}') 400 "Request not found"
T "11.9 Student reject -> 400"       (Req POST "$base/api/admin/requests/reject" $sH "{`"requestId`":$rqId4,`"refusalReason`":`"x`"}") 400 "Only admins can reject"
T "11.10 No token -> 401"            (Req POST "$base/api/admin/requests/reject" @{} "{`"requestId`":$rqId4,`"refusalReason`":`"x`"}") 401 "Unauthorized"

# SUMMARY
Write-Host ""
Write-Host "############################################################"
Write-Host "  SALMA'S TESTS - FINAL: $p PASSED  |  $f FAILED"
Write-Host "############################################################"
if ($f -eq 0) { Write-Host "  ALL TESTS PASSED" } else { Write-Host "  *** SOME TESTS FAILED ***" }
