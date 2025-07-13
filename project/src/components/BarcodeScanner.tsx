import React, { useRef, useEffect, useState } from 'react';
import Webcam from 'react-webcam';
import { BrowserMultiFormatReader } from '@zxing/library';

const BarcodeScanner: React.FC = () => {
  const webcamRef = useRef<Webcam>(null);
  const [scanning, setScanning] = useState(false);
  const [lastScanned, setLastScanned] = useState<string>('');
  const codeReader = useRef(new BrowserMultiFormatReader());

  useEffect(() => {
    let interval: NodeJS.Timeout;

    if (scanning) {
      interval = setInterval(async () => {
        try {
          const webcam = webcamRef.current;
          if (webcam && webcam.getScreenshot()) {
            const imageSrc = webcam.getScreenshot();
            if (imageSrc) {
              const result = await codeReader.current.decodeFromImage(imageSrc);
              if (result && result.text !== lastScanned) {
                setLastScanned(result.text);
                // TODO: Send attendance record to backend
                console.log('Barcode scanned:', result.text);
              }
            }
          }
        } catch (error) {
          // Ignore errors when no barcode is found
        }
      }, 500);
    }

    return () => {
      if (interval) {
        clearInterval(interval);
      }
    };
  }, [scanning, lastScanned]);

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
            onClick={() => setScanning(!scanning)}
            className={`px-4 py-2 rounded-md ${
              scanning
                ? 'bg-red-600 hover:bg-red-700'
                : 'bg-green-600 hover:bg-green-700'
            } text-white`}
          >
            {scanning ? 'Stop Scanning' : 'Start Scanning'}
          </button>
        </div>
      </div>
      {lastScanned && (
        <div className="bg-green-100 border border-green-400 text-green-700 px-4 py-3 rounded">
          <p className="font-medium">Last scanned barcode: {lastScanned}</p>
        </div>
      )}
    </div>
  );
};

export default BarcodeScanner;