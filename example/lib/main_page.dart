import 'dart:async';
import 'dart:io';
import 'package:edge_detection/edge_detection.dart';
import 'package:flutter/material.dart';
import 'package:path/path.dart';
import 'package:path_provider/path_provider.dart';
import 'package:permission_handler/permission_handler.dart';

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  List<String> imageList = [];

  @override
  void initState() {
    imageList.clear();
    super.initState();
  }

  Future<void> getImage() async {
    bool isCameraGranted = await Permission.camera.request().isGranted;
    if (!isCameraGranted) {
      isCameraGranted =
          await Permission.camera.request() == PermissionStatus.granted;
    }

    if (!isCameraGranted) {
      // Have not permission to camera
      return;
    }

// Generate filepath for saving
    imageList.clear();
    for(int i=0; i<5; i++){

        imageList.add(join((await getApplicationSupportDirectory()).path,
            "${(DateTime.now())}${i}.jpeg"));

        // data/user/0/com.sample.edgedetectionexample/files/2023-01-19 14:50:37.1386240.jpeg
    }

    try {
      //Make sure to await the call to detectEdge.
      bool success = await EdgeDetection.detectEdge(
        imageList[0],
        imageList[1],
        imageList[2],
        imageList[3],
        imageList[4],
        canUseGallery: true,
        androidScanTitle: 'Сканирование',
        // use custom localizations for android
        androidCropTitle: 'Редактирование',
        androidCropBlackWhiteTitle: 'Ч/Б',
        androidCropReset: 'Отменить',
      );
    } catch (e) {
      print(e);
    }


    if (!mounted) return;

    setState(() {

    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Corner detection'),
      ),
      body: SingleChildScrollView(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.start,
          children: [

            Container(
              height: 150,
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                crossAxisAlignment: CrossAxisAlignment.center,
                children: [
                  Center(
                    child: ElevatedButton(
                      onPressed: getImage,
                      child: Text('Scan'),
                    ),
                  ),
                  SizedBox(height: 20),

                ],
              ),
            ),
            //  Container(
            //    width: 350,
            //   alignment: Alignment.center,
            //   height: 250,
            //   child: GridView.builder(
            //     gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
            //       crossAxisCount: 3,
            //       crossAxisSpacing: 10,
            //       mainAxisSpacing: 10
            //     ),
            //     itemCount: 5,
            //     itemBuilder: (context, index) {
            //       return imageList.length!=0?
            //      imageWidget(imageList[index]):Container(
            //         decoration: BoxDecoration(
            //           border: Border.all(color: Colors.black,width: 2),
            //           color: Colors.grey
            //         ),
            //         child: Icon(Icons.photo_camera, size: 100,),
            //       );
            //     },
            //   )
            //   ,
            // )
          ],
        ),
      ),
    );
  }


  Widget imageWidget(String imagePath){
    return Container(
        height: 110,
        child: Image.file(
          File(imagePath),
          fit: BoxFit.fill,
          height: 100,
          width: 100,
        )
    );
  }
}
