'use client'

import { useWeather } from '@/contexts/WeatherContext'
import { LiaSlashSolid } from 'react-icons/lia'
import { format } from 'date-fns'
import { ko } from 'date-fns/locale'
import Image from 'next/image'

export default function Weather() {
  const { weather, error, location } = useWeather()

  return (
    <div className="m-4 p-2 flex justify-center bg-gray-light rounded-lg">
      <div className="w-full">
        {error && !weather && <p>Error: {error}</p>}
        {location.latitude === null && location.longitude === null && (
          <p>Loading location...</p>
        )}
        {weather ? (
          <div className="flex flex-col items-center gap-2">
            <div className="flex items-center space-x-2 my-1">
              <span className="text-lg font-semibold">
                {format(weather.date, 'M월 d일', { locale: ko })} (
                {weather.dayOfWeek})
              </span>
              <span className="bg-secondary rounded-full text-white text-xs px-2 py-0.5">
                {weather.season}
              </span>
            </div>
            <div className="flex items-center space-x-10 font-semibold text-lg">
              <div className="flex flex-col items-center min-w-20">
                <span className="text-red-500 me-8">{weather.maxTemp}°</span>
                <LiaSlashSolid className="absolute mt-5 text-secondary" />
                <span className="text-blue-500 ms-12">{weather.minTemp}°</span>
              </div>
              <Image
                src={`/images/weather/${weather.icon}`}
                alt={weather.description}
                width={0}
                height={0}
                sizes="100%"
                className="w-16"
              />
              <div className="text-center min-w-20">
                <p className="text-lg">{weather.description}</p>
                <p className="text-sm">습도 {weather.humidity}%</p>
              </div>
            </div>
          </div>
        ) : (
          location.latitude !== null &&
          location.longitude !== null && <p>Loading weather...</p>
        )}
      </div>
    </div>
  )
}
