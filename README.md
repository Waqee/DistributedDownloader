# DistributedDownloader
Distribute download on multiple android devices.

An ongoing project that distributes download on multiple android phones preferably on different internet connections like 3g.
Uses wifi direct for connection.

On current implementation, you need to build the app through android studio on 2 different android phones.

I will call the phone where file needs to be downloaded as server and the second device as client.

Make a folder named "Downloader" in base directory of both devices.

Enable wifi-direct on both devices if pre android 5. Android 5+ has wifi direct enabled passively.

Go to client screen on the client.

Search for devices.

Touch the name of server device to connect to it. A message pop up will be shown on server device if pre android 5. Accept it.

Now, enter the download link on the server device and press start server button.

Press send button on client device and the download will start.

Completed message will be shown on server device when download completed.

The file will still be divided into parts and will be stored in Downloader Folder.

