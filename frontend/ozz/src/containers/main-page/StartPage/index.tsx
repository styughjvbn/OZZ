'use client'

import Image from 'next/image'
import { useRouter } from 'next/navigation'
import { FaArrowRight } from 'react-icons/fa'
import DEMO_AUTH_CHANGED_EVENT from '@/lib/demoAuthEvents'

const DEMO_AUTH_URL =
  process.env.NEXT_PUBLIC_DEMO_AUTH_URL || 'https://auth.sjw-project.site'

const MOCK_COOKIE_NAME = 'sjw_demo_mock'

const startDemoLogin = async (onSuccess: () => void) => {
  if (window.location.hostname === 'localhost') {
    document.cookie = `${MOCK_COOKIE_NAME}=local-demo; Max-Age=86400; path=/; SameSite=Lax`
  } else {
    await fetch(`${DEMO_AUTH_URL}/api/demo-login`, {
      method: 'POST',
      credentials: 'include',
    })
  }

  const response = await fetch('/api/demo-guest-login', {
    method: 'POST',
    credentials: 'include',
  })

  if (!response.ok) {
    throw new Error('Demo login failed')
  }

  window.dispatchEvent(new Event(DEMO_AUTH_CHANGED_EVENT))
  onSuccess()
}

export default function StartPage() {
  const router = useRouter()

  return (
    <div className="absolute top-0 bg-secondary h-screen w-full z-50 flex flex-col justify-center items-center gap-2">
      <Image
        src="/images/logo_blank_green.png"
        alt="logo"
        width={0}
        height={0}
        sizes="100%"
        className="w-36 h-auto"
      />
      <h1 className="text-3xl text-white font-bold">옷짱 : OZZ</h1>
      <div className="text-primary-400 text-center font-semibold my-16">
        <p>옷을 편하게 관리할 수 있는 웹앱 서비스, 옷짱</p>
        <p>한 번의 클릭으로 디지털 옷장을 관리해보세요!</p>
      </div>
      <div className="absolute bottom-24 w-full px-6">
        <button
          type="button"
          className="relative bg-primary-400 rounded-full p-2 w-full flex items-center justify-center hover:bg-secondary hover:outline hover:outline-primary-400 hover:text-primary-400 transition duration-200"
          onClick={() => startDemoLogin(() => router.push('/closet'))}
        >
          <span className="font-bold text-lg">데모 로그인</span>
          <FaArrowRight className="absolute right-4" />
        </button>
      </div>
    </div>
  )
}
