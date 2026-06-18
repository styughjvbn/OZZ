import { NextRequest, NextResponse } from 'next/server'
import { Api as ClothesApi } from '@/types/clothes/Api'

const MUSINSA_RECOMMEND_URL = 'https://www.musinsa.com/main/musinsa/recommend'
const DEMO_IMPORT_SIZE = 10

interface RecommendedGoods {
  goodsNo: number
  goodsName: string
  brandName: string
  goodsImage: string
}

interface PurchaseHistory {
  name: string
  brand: string
  purchaseDate: string
  purchaseSite: string
  imgUrl: string
  option: string
}

const isRecord = (value: unknown): value is Record<string, unknown> => {
  return typeof value === 'object' && value !== null
}

const isRecommendedGoods = (value: unknown): value is RecommendedGoods => {
  if (!isRecord(value)) return false

  return (
    typeof value.goodsNo === 'number' &&
    typeof value.goodsName === 'string' &&
    typeof value.brandName === 'string' &&
    typeof value.goodsImage === 'string' &&
    value.goodsImage.startsWith('https://image.msscdn.net/')
  )
}

const collectGoods = (value: unknown, goods: RecommendedGoods[]) => {
  if (Array.isArray(value)) {
    value.forEach((item) => collectGoods(item, goods))
    return
  }

  if (!isRecord(value)) return

  if (isRecommendedGoods(value)) {
    goods.push(value)
  }

  Object.values(value).forEach((item) => collectGoods(item, goods))
}

const shuffle = <T>(items: T[]) => {
  const result = [...items]

  for (let index = result.length - 1; index > 0; index -= 1) {
    const randomIndex = Math.floor(Math.random() * (index + 1))
    ;[result[index], result[randomIndex]] = [result[randomIndex], result[index]]
  }

  return result
}

const fetchMusinsaRecommendedGoods = async () => {
  const response = await fetch(MUSINSA_RECOMMEND_URL, {
    cache: 'no-store',
    headers: {
      accept: 'text/html',
      'user-agent':
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/125 Safari/537.36',
    },
  })

  if (!response.ok) {
    throw new Error('Musinsa recommendation page is unavailable')
  }

  const html = await response.text()
  const match = html.match(
    /<script id="__NEXT_DATA__" type="application\/json">(.+?)<\/script>/,
  )

  if (!match) {
    throw new Error('Musinsa recommendation data was not found')
  }

  const goods: RecommendedGoods[] = []
  collectGoods(JSON.parse(match[1]), goods)

  const uniqueGoods = Array.from(
    new Map(goods.map((item) => [item.goodsNo, item])).values(),
  )

  if (uniqueGoods.length < DEMO_IMPORT_SIZE) {
    throw new Error('Not enough Musinsa recommendation data')
  }

  return shuffle(uniqueGoods).slice(0, DEMO_IMPORT_SIZE)
}

const toPurchaseHistory = (goods: RecommendedGoods[]): PurchaseHistory[] => {
  const today = new Date().toISOString().slice(0, 10)

  return goods.map((item) => ({
    name: item.goodsName,
    brand: item.brandName,
    purchaseDate: today,
    purchaseSite: '무신사 추천',
    imgUrl: item.goodsImage,
    option: '데모 추천 상품',
  }))
}

const createClothesApi = (accessToken: string): ClothesApi<unknown> => {
  return new ClothesApi({
    securityWorker: async () => ({
      headers: {
        Authorization: `Bearer ${accessToken}`,
      },
    }),
  })
}

const sendPurchaseHistoryToServer = async (
  purchaseHistory: PurchaseHistory[],
  accessToken: string,
) => {
  const api = createClothesApi(accessToken)
  return api.startBatch(purchaseHistory)
}

// eslint-disable-next-line import/prefer-default-export
export async function POST(req: NextRequest) {
  try {
    const accessToken = req.headers
      .get('cookie')
      ?.split('; ')
      .find((cookie) => cookie.startsWith('access='))
      ?.split('=')[1]

    if (!accessToken) {
      throw new Error('Access token not found')
    }

    const goods = await fetchMusinsaRecommendedGoods()
    const purchaseHistory = toPurchaseHistory(goods)
    const response = await sendPurchaseHistoryToServer(
      purchaseHistory,
      accessToken,
    )

    return NextResponse.json({
      ok: true,
      message:
        '데모에서는 실제 무신사 로그인을 사용하지 않고, 무신사 실시간 추천 상품 10개를 구매내역처럼 가져옵니다.',
      importedCount: purchaseHistory.length,
      response,
    })
  } catch (error) {
    const errorMessage =
      error instanceof Error ? error.message : 'An unknown error occurred.'

    return NextResponse.json({ error: errorMessage }, { status: 500 })
  }
}
