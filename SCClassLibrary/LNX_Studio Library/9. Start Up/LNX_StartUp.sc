  //*************************//
 // LNX_STUDIO Version 1.6  //
//*************************//
//
// start appropriate services (developer or standalone mode)
// remove this if you don't want lnx to auto-start on launch
// LNX_Studio(Server.local); // do this instead for a manual start
//

LNX_StartUp {
	
	classvar >studio;
		
	*initClass{
		Class.initClassTree(LNX_File);
		Platform.case(
			\linux,		{ this.linuxInitClass   },
		);
		StartUp.add {
			Platform.case(
				\osx,		{ this.osxStartUp     },
				\linux,		{ this.linuxStartUp   },
				\windows,	{ this.windowsStartUp }
			);
		};	
		ShutDown.add { studio.onClose };		 // and on shutdown
	}

	*linuxInitClass{
		var lrd = Platform.lnxResourceDir;
		var cwd = File.getcwd;
		if (lrd.pathExists(false).not) {
			lrd.makeDir;
		};
		if ((lrd +/+ "lnx.jpg").pathExists(false).not) {
			File.copy(cwd +/+ "lnx.jpg", lrd +/+ "lnx.jpg");
		};
		if ((lrd +/+ "demo song").pathExists(false).not) {
			File.copy(cwd +/+ "demo song", lrd +/+ "demo song");
		};
		if ((lrd +/+ "sounds").pathExists(false).not) {
			var sounds = PathName(cwd +/+ "sounds").deepFiles;
			sounds.do {|s|
				var newPath = PathName(s.fullPath.replace(cwd, lrd));
				newPath.pathOnly.makeDir;
				File.copy(s.fullPath,
					newPath.fullPath);
			};
		};
	}

	*xPlatStartUp{						
		studio = LNX_Studio(Server.local); 	// start the studio, use local server
//		studio = LNX_Studio(Server.internal); // start the studio, use internal server

		// add appropriate menus
		if (studio.showDev) {
			LNX_AppMenus.addDeveloperMenus(studio);
		};
		if (studio.isStandalone) {
			LNX_AppMenus.addReleaseMenus(studio);
		};

		thisProcess.platform.recordingsDir = (Platform.userHomeDir +/+ "Desktop").standardizePath;
	}

	// TODO: any windows specific startup here
	*windowsStartUp{
		this.xPlatStartUp;
		this.postStartUp;
	}
	
	// TODO: any linux specific startup here
	*linuxStartUp{
		this.xPlatStartUp;
		this.postStartUp;
	}
	
	*osxStartUp{
	
		// bug fix for starting macOS midi and some MIDI devices
		var midiBugFix = ("midiBugFix".loadPref ? [false])[0].isTrue; // get preference
		var audioRunning = "Audio".pid.notNil;    // is Audio MIDI Setup running?
		if (audioRunning.not  && midiBugFix) {
			"Audio MIDI Setup".openApplication;  // if not open it
		}; 
		SCDoc.indexAllDocuments;

		if (audioRunning) {
			this.osxStartUp2;
		}{
			{ this.osxStartUp2 }.defer(2); // delay start-up so Audio MIDI Setup starts 1st
		}; 
	}
	
	*osxStartUp2{
		
		"".postln;
		"".postln;
		"If this application repeatedly hangs on opening the following might help...".postln;
		"===========================================================================".postln;
		"1. In the preferences of the Mac-OS 'Audio MIDI Setup' application choose".postln;
		"      'When application launches open MIDI Window'".postln;
		"2. Remove all MIDI devices & Relaunch LNX_Studio".postln;
		"3. Open LNX_Studio preferences & Turn on MacOS [MIDI Fix]".postln;
		"4. Plug all MIDI equipment back in".postln;
		"5. Quit & Relaunch LNX_Studio".postln;
		"".postln;
				
		studio = LNX_Studio(Server.local); 	// start the studio, use local server
//		studio = LNX_Studio(Server.internal); // start the studio, use internal server
				
		LNX_AppMenus.addReleaseMenus(studio);

		// to remove for release
		LNX_AppMenus.addDeveloperMenus(studio); 
		Document.listener.bounds=Rect(LNX_Studio.osx,32,535,175);
		
		// to uncomment for release
//		Document.listener.close;				
//		Document.initAction=Document.initAction.addFunc{|doc|
//			if	(studio.isStandalone) {
//				doc.editable_(false)
//			};
//		};
					
		// load songs dropped or opened in SC
		Document.initAction=Document.initAction.addFunc{|doc|
			if (doc.string[..14]=="SC Studio Doc v") {
				{
					doc.bounds_(Rect(0,0,0,0));
				}.defer(0.05);
				{
					studio.dropLoad(doc);
				}.defer(0.1);
				{
					doc.close
				}.defer(0.15)
			};
		};
		
		// load songs dropped or opened in SC at start-up
		Document.allDocuments.do{|doc|
			if (doc.string[..14]=="SC Studio Doc v") {
				{
					doc.bounds_(Rect(0,0,0,0));
				}.defer(0.05);
				{
					studio.dropLoad(doc);
				}.defer(0.1);
				{
					doc.close
				}.defer(0.15)
			};
		};
		
		// add lnx preferences as the preferences
		thisProcess.preferencesAction = { studio.preferences };
		
		if (studio.isStandalone && LNX_Mode.isSafe) {
			thisProcess.platform.recordingsDir = 
				"/Users/Shared";
				//String.scDir.dirname.dirname +/+ "Contents/Safe Mode Recordings";
		}{
			// set the recording directory
			thisProcess.platform.recordingsDir = "~/Desktop".standardizePath;
		};
		
		this.postStartUp;
	}
	
	*postStartUp{
		this.interpreterDebugging;
	}
	
	*interpreterDebugging{
		// attach various objects to interpreter variables for debugging
		"	
			a=LNX_StartUp.studio;
			s=a.server;
		 	n=a.network;
		 	u=n.thisUser;
		 	i=a.insts;
		 	//p=LNX_API(a,'test');
		".interpret;		
	}
	
	*studio{^studio} // to remove for release

}

