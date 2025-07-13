import React, { useRef, useEffect, useState } from 'react';
import Webcam from 'react-webcam';
import * as faceapi from 'face-api.js';

const FaceRecognition: React.FC = () => {
  const webcamRef = useRef<Webcam>(null);
  const [isModelLoaded, setIsModelLoaded] = useState(false);
  const [detecting, setDetecting] = useState(false);
  const [detectedFace, setDetectedFace] = useState<string | null>(null);

  useEffect(() => {
    const loadModels = async () => {
      try {
        await Promise.all([
          faceapi.nets.tinyFaceDetector.loadFromUri('/models'),
          faceapi.nets.faceLandmark68Net.loadFromUri('/models'),
          faceapi.nets.faceRecognitionNet.loadFromUri('/models')
        ]);
        setIsModelLoaded(true);
      } catch (error) {
        console.error('Error loading face detection models:', error);
      }
    };

    loadModels();
  }, []);

  useEffect(() => {
    let interval: NodeJS.Timeout;

    if (detecting && isModelLoaded) {
      interval = setInterval(async () => {
        try {
          const webcam = webcamRef.current;
          if (webcam && webcam.getScreenshot()) {
            const img = await faceapi.fetchImage(webcam.getScreenshot()!);
            const detections = await faceapi
              .detectAllFaces(img, new faceapi.TinyFaceDetectorOptions())
              .withFaceLandmarks();

            if (detections.length > 0) {
              // TODO: Implement face recognition and matching
              setDetectedFace('Face detected!');
            } else {
              setDetectedFace(null);
            }
          }
        } catch (error) {
          console.error('Face detection error:', error);
        }
      }, 1000);
    }

    return () => {
      if (interval) {
        clearInterval(interval);
      }
    };
  }, [detecting, isModelLoaded]);

  return (
    <div className="space-y-4">
      <div className="relative">
        <Webcam
          ref={webcamRef}
          className="w-full max-w-2xl mx-auto rounded-lg shadow-lg"
          screenshotFormat="image/jpeg"
        />
        <div className="absolute top-4 right-4">
          <button
            onClick={() => setDetecting(!detecting)}
            disabled={!isModelLoaded}
            className={`px-4 py-2 rounded-md ${
              !isModelLoaded
                ? 'bg-gray-400'
                : detecting
                ? 'bg-red-600 hover:bg-red-700'
                : 'bg-green-600 hover:bg-green-700'
            } text-white`}
          >
            {!isModelLoaded
              ? 'Loading models...'
              : detecting
              ? 'Stop Detection'
              : 'Start Detection'}
          </button>
        </div>
      </div>
      {detectedFace && (
        <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded">
          <p className="font-medium">{detectedFace}</p>
        </div>
      )}
    </div>
  );
};

export default FaceRecognition;