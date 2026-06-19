'use client'

import { useState } from 'react'
import { useRouter } from 'next/navigation'
import Loading from '@/app/closet/loading'
import Image from 'next/image'
import AlertModal from '@/components/Modal/AlertModal'

const IMPORT_TIMEOUT_MS = 120000

export function DemoImportForm({ mall }: { mall: string }) {
  const router = useRouter()
  const [isLoading, setIsLoading] = useState(false)
  const [isAlertOpen, setIsAlertOpen] = useState(false)
  const [alertMessage, setAlertMessage] = useState<string[]>([])

  async function onSubmit() {
    setIsLoading(true)
    const controller = new AbortController()
    const timeout = setTimeout(() => controller.abort(), IMPORT_TIMEOUT_MS)

    try {
      const response = await fetch(`/apis/${mall}-login`, {
        method: 'POST',
        signal: controller.signal,
      })
      const result = await response.json()

      if (!response.ok || result.error) {
        setAlertMessage(['추천 상품을', '가져오지 못했습니다.'])
        setIsAlertOpen(true)
        return
      }

      router.push('/closet')
    } catch (error) {
      setAlertMessage(
        error instanceof DOMException && error.name === 'AbortError'
          ? ['추천 상품 가져오기가', '시간 초과되었습니다.']
          : ['추천 상품을', '가져오지 못했습니다.'],
      )
      setIsAlertOpen(true)
    } finally {
      clearTimeout(timeout)
      setIsLoading(false)
    }
  }

  if (isLoading) {
    return <Loading />
  }

  return (
    <div className="w-2/3 space-y-4">
      <p className="text-sm leading-5 text-neutral-600">
        데모에서는 실제 무신사 로그인을 사용하지 않고, 무신사 실시간 추천 상품
        3개를 AI가 자동 분석해 가져옵니다.
      </p>
      <button
        type="button"
        onClick={onSubmit}
        className="bg-black text-white rounded-sm py-2 w-full"
      >
        추천 상품 가져오기
      </button>

      {isAlertOpen && (
        <AlertModal
          onClose={() => setIsAlertOpen(false)}
          messages={alertMessage}
        />
      )}
    </div>
  )
}

export default function MallLoginForm({ mall }: { mall: string }) {
  return (
    <div className="flex flex-col justify-center items-center">
      <div className="my-1 w-full h-10 px-6 py-2 flex items-center justify-center bg-black text-white">
        <Image
          src={`/images/malls/logo_${mall}.png`}
          alt={mall.toUpperCase()}
          width={0}
          height={0}
          sizes="100%"
          className="h-3 w-auto pe-0.5"
        />
        에서 옷 가져오기
      </div>
      <h1 className="text-lg my-3">데모 가져오기</h1>
      <DemoImportForm mall={mall} />
    </div>
  )
}
