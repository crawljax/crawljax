#NoTrayIcon
SetTitleMatchMode, 1
WinWait, %1% ahk_class MozillaDialogClass , ,1
WinKill, %1% ahk_class MozillaDialogClass 
