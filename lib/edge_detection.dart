import 'dart:async';

import 'package:flutter/services.dart';

class EdgeDetection {
  static const MethodChannel _channel = const MethodChannel('edge_detection');

  static Future<bool> detectEdge(
      String saveTo1,
      String saveTo2,
      String saveTo3,
      String saveTo4,
      String saveTo5,
      {bool canUseGallery: true,
        String androidScanTitle: "Scanning",
        String androidCropTitle: "Crop",
        String androidCropBlackWhiteTitle: "Black White",
        String androidCropReset: "Reset"}) async {
    return await _channel.invokeMethod('edge_detect', {
      'save_to1': saveTo1,
      'save_to2': saveTo2,
      'save_to3': saveTo3,
      'save_to4': saveTo4,
      'save_to5': saveTo5,
      'can_use_gallery': canUseGallery,
      'scan_title': androidScanTitle,
      'crop_title': androidCropTitle,
      'crop_black_white_title': androidCropBlackWhiteTitle,
      'crop_reset_title': androidCropReset,
    });
  }

  static Future<bool> detectEdgeFromGallery(String saveTo,
      {String androidCropTitle: "Crop",
        String androidCropBlackWhiteTitle: "Black White",
        String androidCropReset: "Reset"}) async {
    return await _channel.invokeMethod('edge_detect_gallery', {
      'save_to': saveTo,
      'crop_title': androidCropTitle,
      'crop_black_white_title': androidCropBlackWhiteTitle,
      'crop_reset_title': androidCropReset,
    });
  }
}
