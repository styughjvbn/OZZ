import { NextRequest, NextResponse } from 'next/server'

const RANKING_URL =
  'https://client.musinsa.com/api/home/web/v5/pans/ranking?storeCode=musinsa&sectionId=199&skip_bf=Y&gf=A&contentsId=&categoryCode=000&ageBand=AGE_BAND_ALL'
const GOODS_API_URL = 'https://api.musinsa.com/api2/dp/v1/goods'
const OPTIONS_API_URL = 'https://goods-detail.musinsa.com/api2/goods'
const IMPORT_SIZE = 3
const API_GATEWAY_URL = process.env.API_GATEWAY_URL || 'http://localhost:8000'
const REQUEST_TIMEOUT_MS = 15000
const BATCH_TIMEOUT_MS = 120000
const BROWSER_HEADERS = {
  accept: 'application/json',
  'user-agent':
    'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125 Safari/537.36',
}

interface PurchaseHistory {
  name: string
  brand: string
  purchaseDate: string
  purchaseSite: string
  imgUrl: string
  option: string
}

interface GoodsDetail {
  goodsNo: number
  goodsName: string
  brandName: string
  imageUrl: string
}

const isRecord = (value: unknown): value is Record<string, unknown> => {
  return typeof value === 'object' && value !== null
}

const isGoodsDetail = (value: unknown): value is GoodsDetail => {
  if (!isRecord(value)) return false

  return (
    typeof value.goodsNo === 'number' &&
    typeof value.goodsName === 'string' &&
    typeof value.brandName === 'string' &&
    typeof value.imageUrl === 'string'
  )
}

const collectGoodsNos = (value: unknown, goodsNos: number[]) => {
  if (Array.isArray(value)) {
    value.forEach((item) => collectGoodsNos(item, goodsNos))
    return
  }

  if (!isRecord(value)) return

  if (value.type === 'PRODUCT_COLUMN') {
    if (typeof value.id === 'number') {
      goodsNos.push(value.id)
    }

    if (typeof value.id === 'string' && /^\d+$/.test(value.id)) {
      goodsNos.push(Number(value.id))
    }
  }

  Object.values(value).forEach((item) => collectGoodsNos(item, goodsNos))
}

const shuffle = <T>(items: T[]) => {
  return items
    .map((item) => ({ item, order: Math.random() }))
    .sort((a, b) => a.order - b.order)
    .map(({ item }) => item)
}

const fetchRankingGoodsNos = async () => {
  const response = await fetch(RANKING_URL, {
    cache: 'no-store',
    headers: BROWSER_HEADERS,
  })

  if (!response.ok) {
    throw new Error('Failed to fetch Musinsa ranking data')
  }

  const data = await response.json()
  const goodsNos: number[] = []
  collectGoodsNos(data, goodsNos)

  const uniqueGoodsNos = shuffle(Array.from(new Set(goodsNos))).slice(
    0,
    IMPORT_SIZE,
  )

  if (uniqueGoodsNos.length < IMPORT_SIZE) {
    throw new Error('Not enough Musinsa ranking data')
  }

  return uniqueGoodsNos
}

const fetchGoodsDetails = async (goodsNos: number[]) => {
  const params = new URLSearchParams({
    goodsNoList: goodsNos.join(','),
    saleStateList: 'SALE,SOLD_OUT',
  })
  const response = await fetch(`${GOODS_API_URL}?${params}`, {
    cache: 'no-store',
    headers: BROWSER_HEADERS,
  })

  if (!response.ok) {
    throw new Error('Failed to fetch Musinsa goods details')
  }

  const data = await response.json()
  const list: unknown[] =
    isRecord(data.data) && Array.isArray(data.data.list) ? data.data.list : []
  const details = list.filter(isGoodsDetail)
  const detailsByGoodsNo = new Map<number, GoodsDetail>(
    details.map((item) => [item.goodsNo, item]),
  )

  return goodsNos
    .map((goodsNo) => detailsByGoodsNo.get(goodsNo))
    .filter((item): item is GoodsDetail => Boolean(item))
}

const getOptionNamesFromValues = (value: unknown): string[] => {
  if (!Array.isArray(value)) return []

  return value
    .filter(isRecord)
    .filter((item) => item.isDeleted !== true)
    .map((item) => item.name)
    .filter((name): name is string => typeof name === 'string')
}

const getPrimaryOptionFromItems = (value: unknown): string | null => {
  if (!Array.isArray(value) || value.length === 0) return null

  const records = value.filter(isRecord)
  const target = records.find((item) => item.activated === true) || records[0]
  const names = getOptionNamesFromValues(target?.optionValues)

  if (names.length > 0) {
    return names.join('/')
  }

  return typeof target?.managedCode === 'string' ? target.managedCode : null
}

const getPrimaryOptionFromBasicOptions = (value: unknown): string | null => {
  if (!Array.isArray(value) || value.length === 0) return null

  const optionNames = value
    .filter(isRecord)
    .map((optionGroup) => {
      const names = getOptionNamesFromValues(optionGroup.optionValues)
      return names[0]
    })
    .filter((name): name is string => typeof name === 'string')

  return optionNames.length > 0 ? optionNames.join('/') : null
}

const fetchPrimaryOption = async (goodsNo: number) => {
  const response = await fetch(`${OPTIONS_API_URL}/${goodsNo}/options`, {
    cache: 'no-store',
    headers: BROWSER_HEADERS,
  })

  if (!response.ok) {
    return '단품'
  }

  const data = await response.json()
  const optionItemsFromData = isRecord(data.data)
    ? getPrimaryOptionFromItems(data.data.optionItems)
    : null
  const optionItemsFromRoot = getPrimaryOptionFromItems(data.optionItems)
  const basicOptions = isRecord(data.data)
    ? getPrimaryOptionFromBasicOptions(data.data.basic)
    : null

  return optionItemsFromData || optionItemsFromRoot || basicOptions || '단품'
}

const scrapeMusinsaRanking = async (): Promise<PurchaseHistory[]> => {
  const goodsNos = await fetchRankingGoodsNos()
  const details = await fetchGoodsDetails(goodsNos)
  const today = new Date().toISOString().slice(0, 10)

  return Promise.all(
    details.slice(0, IMPORT_SIZE).map(async (item) => ({
      name: item.goodsName,
      brand: item.brandName,
      purchaseDate: today,
      purchaseSite: '무신사',
      imgUrl: item.imageUrl,
      option: await fetchPrimaryOption(item.goodsNo),
    })),
  )
}

const withTimeout = async <T>(
  run: (signal: AbortSignal) => Promise<T>,
  timeoutMs = REQUEST_TIMEOUT_MS,
) => {
  const controller = new AbortController()
  const timeout = setTimeout(() => controller.abort(), timeoutMs)

  try {
    return await run(controller.signal)
  } finally {
    clearTimeout(timeout)
  }
}

const sendPurchaseHistoryToServer = async (
  purchaseHistory: PurchaseHistory[],
  accessToken: string,
) => {
  const response = await withTimeout(
    (signal) =>
      fetch(`${API_GATEWAY_URL}/api/clothes/batch`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${accessToken}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(purchaseHistory),
        signal,
      }),
    BATCH_TIMEOUT_MS,
  )

  if (!response.ok) {
    const message = await response.text()
    throw new Error(message || 'Failed to send purchase history to the server')
  }

  const reader = response.body?.getReader()

  if (reader) {
    await withTimeout(async () => {
      await reader.read()
    }, BATCH_TIMEOUT_MS)
    reader.releaseLock()
    await response.body?.cancel()
  }

  return {
    importedCount: purchaseHistory.length,
    failedCount: 0,
    responseStatus: response.status,
  }
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

    const purchaseHistory = await scrapeMusinsaRanking()
    const response = await sendPurchaseHistoryToServer(
      purchaseHistory,
      accessToken,
    )

    return NextResponse.json({
      ok: true,
      message:
        '실시간 무신사 랭킹 상품 3개를 추출하여 구매내역처럼 가져왔습니다.',
      importedCount: response.importedCount,
      failedCount: response.failedCount,
      response,
    })
  } catch (error) {
    const errorMessage =
      error instanceof Error ? error.message : 'An unknown error occurred.'

    return NextResponse.json({ error: errorMessage }, { status: 500 })
  }
}
