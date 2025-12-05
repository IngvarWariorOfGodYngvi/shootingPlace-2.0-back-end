param([string]$outputPath = "..json")
$Url = "https://ksdziesiatka.pl/old/wp-content/uploads/2025/06/shootingplace-1.0.war"
$Response = Invoke-WebRequest -Uri $Url -Method POST -ContentType "application/json"
$Parsed = $Response.Content | ConvertFrom-Json
$Parsed | Add-Member -NotePropertyName "updateDate" -NotePropertyValue (Get-Date -Format "dd.MM.yyyy")
$Parsed | ConvertTo-Json -Depth 10 | Out-File -Encoding utf8 $outputPath