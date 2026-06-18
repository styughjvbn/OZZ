// @ts-nocheck
/* eslint-disable */
import { NextRequest, NextResponse } from 'next/server'
import { Api as ClothesApi } from '@/types/clothes/Api'

/**
 * This API route replaces the original Musinsa recommendation scraper with a
 * scraper that pulls the current Musinsa ranking (sectionId=199) and then
 * enriches each product with its primary option. It selects the first
 * activated option (or the first option item if none are marked as
 * activated) and joins the option values with a slash. The resulting
 * products are then mapped into the PurchaseHistory structure and sent
 * through the ClothesApi.
 */

const RANKING_URL =
  'https://client.musinsa.com/api/home/web/v5/pans/ranking?storeCode=musinsa&sectionId=199&skip_bf=Y&gf=A&contentsId=&categoryCode=000&ageBand=AGE_BAND_ALL'
const GOODS_API_URL = 'https://api.musinsa.com/api2/dp/v1/goods'
const OPTIONS_API_TEMPLATE =
  'https://goods-detail.musinsa.com/api2/goods/{goodsNo}/options'

const IMPORT_SIZE = 10

/**
 * PurchaseHistory describes the shape expected by the ClothesApi. It
 * contains the product name, brand, image URL, purchase date, purchase
 * site, and option string. The option string is derived from Musinsa's
 * options API and may include colour and size information.
 */
interface PurchaseHistory {
  name: string
  brand: string
  purchaseDate: string
  purchaseSite: string
  imgUrl: string
  option: string | null
}

/**
 * Recursively traverses the ranking response and collects goods IDs from
 * objects whose `type` field is `PRODUCT_COLUMN`. Musinsa's ranking API
 * nests these objects deeply, so a recursive search is necessary.
 */
const collectGoodsIds = (value: unknown, goodsIds: number[]): void => {
  if (Array.isArray(value)) {
    value.forEach((item) => collectGoodsIds(item, goodsIds))
    return
  }
  if (typeof value !== 'object' || value === null) return
  const record = value as Record<string, unknown>
  if (record.type === 'PRODUCT_COLUMN' && typeof record.id === 'number') {
    goodsIds.push(record.id)
  }
  Object.values(record).forEach((v) => collectGoodsIds(v, goodsIds))
}

/**
 * Fetches the current Musinsa ranking and returns the first IMPORT_SIZE
 * unique goods numbers. A custom User-Agent header is used to mimic a
 * browser; without it Musinsa may return a 403.
 */
const fetchRankingGoodsNos = async (): Promise<number[]> => {
  const resp = await fetch(RANKING_URL, {
    headers: {
      // Musinsa APIs are sensitive to User-Agent headers. Use a modern
      // browser signature to avoid being blocked.
      'User-Agent':
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125 Safari/537.36',
      Accept: 'application/json',
    },
    cache: 'no-store',
  })
  if (!resp.ok) {
    throw new Error('Failed to fetch Musinsa ranking data')
  }
  const data = await resp.json()
  const goodsIds: number[] = []
  collectGoodsIds(data, goodsIds)
  // Deduplicate while preserving order
  const uniqueIds: number[] = []
  for (const id of goodsIds) {
    if (!uniqueIds.includes(id)) uniqueIds.push(id)
    if (uniqueIds.length >= IMPORT_SIZE) break
  }
  if (uniqueIds.length < IMPORT_SIZE) {
    throw new Error('Not enough goods in Musinsa ranking to satisfy import size')
  }
  return uniqueIds
}

/**
 * Retrieves detailed product information (name, brand, image) for a list
 * of goods numbers. The Musinsa goods API accepts multiple goods numbers
 * separated by commas. This function returns an array in the same order
 * as the provided goodsNos list.
 */
const fetchGoodsDetails = async (
  goodsNos: number[],
): Promise<
  {
    goodsNo: number
    goodsName: string
    brandName: string
    imageUrl: string
  }[]
> => {
  const params = new URLSearchParams()
  params.set('goodsNoList', goodsNos.join(','))
  params.set('saleStateList', 'SALE,SOLD_OUT')
  const resp = await fetch(`${GOODS_API_URL}?${params.toString()}`, {
    headers: {
      'User-Agent':
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125 Safari/537.36',
      Accept: 'application/json',
    },
    cache: 'no-store',
  })
  if (!resp.ok) {
    throw new Error('Failed to fetch goods details from Musinsa')
  }
  const json = await resp.json()
  const list = (json?.data?.list ?? []) as unknown[]
  const result: {
    goodsNo: number
    goodsName: string
    brandName: string
    imageUrl: string
  }[] = []
  for (const item of list) {
    if (
      typeof item === 'object' &&
      item !== null &&
      typeof (item as any).goodsNo === 'number'
    ) {
      result.push({
        goodsNo: (item as any).goodsNo,
        goodsName: (item as any).goodsName as string,
        brandName: (item as any).brandName as string,
        imageUrl: (item as any).imageUrl as string,
      })
    }
  }
  return result
}

/**
 * Fetches option information for a single goods number. Musinsa returns
 * arrays of optionItems; we select the first item with `activated: true`
 * or fallback to the first item. Option values are joined with a slash.
 */
const fetchPrimaryOption = async (goodsNo: number): Promise<string | null> => {
  const url = OPTIONS_API_TEMPLATE.replace('{goodsNo}', String(goodsNo))
  const resp = await fetch(url, {
    headers: {
      'User-Agent':
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125 Safari/537.36',
      Accept: 'application/json',
    },
    cache: 'no-store',
  })
  if (!resp.ok) {
    // Some goods may lack an option API (single-size goods), so return null
    return null
  }
  const json = await resp.json()
  const optionItems =
    json?.data?.optionItems ?? json?.optionItems ?? ([] as unknown[])
  if (!Array.isArray(optionItems) || optionItems.length === 0) {
    return null
  }
  // find first activated item
  let targetItem: any = optionItems.find((it: any) => it.activated)
  if (!targetItem) targetItem = optionItems[0]
  const values = targetItem?.optionValues as unknown[]
  if (Array.isArray(values) && values.length > 0) {
    const names = values
      .filter((v) => typeof (v as any).name === 'string')
      .map((v) => (v as any).name as string)
    return names.join('/')
  }
  return targetItem?.managedCode ?? null
}

/**
 * Builds an array of PurchaseHistory objects by scraping Musinsa ranking,
 * goods details, and primary options. The purchaseDate is set to today
 * (YYYY-MM-DD). purchaseSite is fixed as "무신사" to reflect the actual
 * source rather than the old demo value.
 */
const scrapeMusinsaRanking = async (): Promise<PurchaseHistory[]> => {
  const goodsNos = await fetchRankingGoodsNos()
  const details = await fetchGoodsDetails(goodsNos)
  const today = new Date().toISOString().slice(0, 10)
  const histories: PurchaseHistory[] = []
  for (const item of details) {
    const option = await fetchPrimaryOption(item.goodsNo)
    histories.push({
      name: item.goodsName,
      brand: item.brandName,
      purchaseDate: today,
      purchaseSite: '무신사',
      imgUrl: item.imageUrl,
      option: option ?? '단품',
    })
  }
  return histories
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
    const purchaseHistory = await scrapeMusinsaRanking()
    const response = await sendPurchaseHistoryToServer(purchaseHistory, accessToken)
    return NextResponse.json({
      ok: true,
      message:
        '실시간 무신사 랭킹 상품 10개를 추출하여 구매내역처럼 가져왔습니다. 옵션 정보는 API에서 추출한 실제 값입니다.',
      importedCount: purchaseHistory.length,
      response,
    })
  } catch (error) {
    const errorMessage =
      error instanceof Error ? error.message : 'An unknown error occurred.'
    return NextResponse.json({ error: errorMessage }, { status: 500 })
  }
}
