import React, { useState, useEffect } from 'react';
import { Camera, UserCheck, MapPin } from 'lucide-react';
import BarcodeScanner from './components/BarcodeScanner';
import FaceRecognition from './components/FaceRecognition';
import LocationCheck from './components/LocationCheck';

function App() {
  const [verificationMethod, setVerificationMethod] = useState<'barcode' | 'face' | 'location'>('barcode');
  const [isAuthenticated, setIsAuthenticated] = useState(false);

  useEffect(() => {
    // Check if user is authenticated
    const checkAuth = async () => {
      // TODO: Implement actual authentication check
      setIsAuthenticated(true);
    };
    checkAuth();
  }, []);

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen bg-gray-100 flex items-center justify-center">
        <div className="bg-white p-8 rounded-lg shadow-md">
          <p className="text-lg">Please log in to continue</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-100">
      <header className="bg-white shadow-sm">
        <div className="max-w-7xl mx-auto px-4 py-4 sm:px-6 lg:px-8">
          <h1 className="text-2xl font-bold text-gray-900">Attendance System</h1>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 py-8 sm:px-6 lg:px-8">
        <div className="bg-white rounded-lg shadow-md p-6">
          <div className="flex space-x-4 mb-6">
            <button
              onClick={() => setVerificationMethod('barcode')}
              className={`flex items-center px-4 py-2 rounded-md ${
                verificationMethod === 'barcode'
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              <Camera className="w-5 h-5 mr-2" />
              Barcode
            </button>
            <button
              onClick={() => setVerificationMethod('face')}
              className={`flex items-center px-4 py-2 rounded-md ${
                verificationMethod === 'face'
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              <UserCheck className="w-5 h-5 mr-2" />
              Face
            </button>
            <button
              onClick={() => setVerificationMethod('location')}
              className={`flex items-center px-4 py-2 rounded-md ${
                verificationMethod === 'location'
                  ? 'bg-blue-600 text-white'
                  : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
              }`}
            >
              <MapPin className="w-5 h-5 mr-2" />
              Location
            </button>
          </div>

          <div className="mt-6">
            {verificationMethod === 'barcode' && <BarcodeScanner />}
            {verificationMethod === 'face' && <FaceRecognition />}
            {verificationMethod === 'location' && <LocationCheck />}
          </div>
        </div>
      </main>
    </div>
  );
}

export default App;