'use client'

import Image from 'next/image'

export default function SignIn() {
  return (
    <div className="flex min-h-[calc(100vh-9rem)] max-w-xs mx-auto flex-col items-center justify-center px-4 text-center">
      <Image
        src="/images/logo_3e3e3e.png"
        alt="OZZ LOGO"
        width={160}
        height={190}
        className="mb-8"
      />
      <h1 className="text-2xl font-extrabold text-[#3e3e3e]">OZZ 데모</h1>
      <p className="mt-4 text-sm font-semibold leading-6 text-[#7A7C7E]">
        공통 데모 계정으로 옷장, 코디 추천, 가상 피팅 흐름을 확인합니다.
      </p>
    </div>
  )
}
