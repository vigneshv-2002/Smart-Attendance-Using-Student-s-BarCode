import React, { useState, useEffect } from 'react';
import { MapPin } from 'lucide-react';

const LocationCheck: React.FC = () => {
  const [location, setLocation] = useState<GeolocationPosition | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [checking, setChecking] = useState(false);

  // Example allowed location (you should configure this based on your needs)
  const allowedLocation = {
    latitude: 40.7128,
    longitude: -74.0060,
    radius: 100 // meters
  };

  const checkLocation = () => {
    setChecking(true);
    setError(null);

    if (!navigator.geolocation) {
      setError('Geolocation is not supported by your browser');
      setChecking(false);
      return;
    }

    navigator.geolocation.getCurrentPosition(
      (position) => {
        setLocation(position);
        setChecking(false);
      },
      (error) => {
        setError('Error getting location: ' + error.message);
        setChecking(false);
      },
      {
        enableHighAccuracy: true,
        timeout: 5000,
        maximumAge: 0
      }
    );
  };

  const calculateDistance = (lat1: number, lon1: number, lat2: number, lon2: number) => {
    const R = 6371e3; // Earth's radius in meters
    const φ1 = (lat1 * Math.PI) / 180;
    const φ2 = (lat2 * Math.PI) / 180;
    const Δφ = ((lat2 - lat1) * Math.PI) / 180;
    const Δλ = ((lon2 - lon1) * Math.PI) / 180;

    const a =
      Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
      Math.cos(φ1) * Math.cos(φ2) * Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return R * c;
  };

  const isWithinAllowedArea = () => {
    if (!location) return false;

    const distance = calculateDistance(
      location.coords.latitude,
      location.coords.longitude,
      allowedLocation.latitude,
      allowedLocation.longitude
    );

    return distance <= allowedLocation.radius;
  };

  return (
    <div className="space-y-6">
      <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center space-x-2">
            <MapPin className="w-5 h-5 text-blue-600" />
            <h2 className="text-lg font-medium">Location Verification</h2>
          </div>
          <button
            onClick={checkLocation}
            disabled={checking}
            className={`px-4 py-2 rounded-md ${
              checking
                ? 'bg-gray-400'
                : 'bg-blue-600 hover:bg-blue-700'
            } text-white`}
          >
            {checking ? 'Checking...' : 'Check Location'}
          </button>
        </div>

        {location && (
          <div className={`p-4 rounded-md ${
            isWithinAllowedArea()
              ? 'bg-green-100 text-green-700'
              : 'bg-red-100 text-red-700'
          }`}>
            <p className="font-medium">
              {isWithinAllowedArea()
                ? 'You are within the allowed area'
                : 'You are outside the allowed area'}
            </p>
            <p className="text-sm mt-1">
              Your location: {location.coords.latitude.toFixed(6)},{' '}
              {location.coords.longitude.toFixed(6)}
            </p>
          </div>
        )}

        {error && (
          <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
            <p>{error}</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default LocationCheck;