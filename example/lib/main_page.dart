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
  String? _imagePath;
  List<String> imageList = [];

  @override
  void initState() {
    super.initState();
  }

  Future<void> getImage() async {
    imageList.map((e) => {print(e)});
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
    String imagePath = join((await getApplicationSupportDirectory()).path,
        "${(DateTime.now().millisecondsSinceEpoch / 1000).round()}.jpeg");

    try {
      //Make sure to await the call to detectEdge.
      bool success = await EdgeDetection.detectEdge(
        imagePath,
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

    // If the widget was removed from the tree while the asynchronous platform
    // message was in flight, we want to discard the reply rather than calling
    // setState to update our non-existent appearance.
    if (!mounted) return;

    setState(() {

      _imagePath = imagePath;
      imageList.add(_imagePath!);
    });
  }

  @override
  Widget build(BuildContext context) {
    Size size = MediaQuery.of(context).size;
    return Scaffold(
      appBar: AppBar(
        title: const Text('Corner detection'),
      ),
      body: SingleChildScrollView(
        child: Column(
          children: [
            Container(
              // height: size.height,
              // width: size.width,
              color: Colors.black.withOpacity(0.1),
            ),
            Container(
              height: 300,
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
                  _imagePath != null ? Text('Cropped image path:') : SizedBox(),
                  _imagePath != null
                      ? Padding(
                          padding:
                              const EdgeInsets.only(top: 0, left: 0, right: 0),
                          child: Text(
                            _imagePath.toString(),
                            textAlign: TextAlign.center,
                            style: TextStyle(fontSize: 14),
                          ),
                        )
                      : SizedBox(),
                ],
              ),
            ),
             Container(
               width: 200,
              alignment: Alignment.center,
              height: 300,
              child: imageList.length!=0?ListView.builder(
                itemCount: imageList.length,
                itemBuilder: (context, index) {
                  return Container(
                    height: 100,
                    child:
                    Image.file(
                      File(imageList[index]),
                    ),
                  );
                },
              ):SizedBox()
              ,
            )
          ],
        ),
      ),
    );
  }
}
