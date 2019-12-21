
Cac buoc Connect Service:
	TH1: service chua chay, activity khoi tao lan dau
		- OnCreate()
			startService()
			bindService()
				-> connected()
					if(!serviceRunning){
						->
						serviceRunning = true
					}else
					{
					}

	TH2: service chay roi. activity khoi tao
		- onCreate()
			bindService()
				-> connected()		
					if(!serviceRunning){
					}else
					{
						->
					}



1. load songlist vao activity: contentResolver -> MusicLoadingModel
2. start service va dua danh sach nhac cho no
3. phat nhac bang playMusicPlayer()
4. Update view tren actvity 

5. Notification <----> Service <----> activity
	- tao notification
	- dieu khien service
	- update view

6. favourite song
	an nut like
		-> databasePresenter.likedOrUnlike()  
		-> model.insert/delete (song) 
		-> updateViewOnLikeOrUnlike()
	
,playlist

	+ an nut tao playlist:
	+ bat dialog de nhap ten
	+ chuyen sang activity chon bai hat
	+ nguoi dung chon bai hat -> selectedSongs -> ket thuc activity-> tra selectedSongs ve 
	+ tao playlist bang selectedSongs
, Album, artist

7. Nhung cai khac: search, dark mode, sleep timer, shuffle, repeat
fix bug


RemoteViews <----> service/activity (dang ky lang nghe su kien cua intent) : if(intent == "PLAY") play()
	Button: intent("PLAY")->Android



onStartCommand(intent){
	
}




