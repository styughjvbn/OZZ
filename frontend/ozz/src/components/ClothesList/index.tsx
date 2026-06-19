import Link from 'next/link'
import Image from 'next/image'
import { useSelectedItem } from '@/contexts/SelectedItemContext'
import { ClothesBasicWithFileResponse } from '@/types/clothes/data-contracts'

interface ClothesListProps {
  clothingList: (ClothesBasicWithFileResponse & { imageUrl: string | null })[]
  isSelectable: boolean
}

const formatDate = (dateString: string): string => {
  const date = new Date(dateString)
  return date.toISOString().split('T')[0]
}

export default function ClothesList({
  clothingList,
  isSelectable,
}: ClothesListProps) {
  const { selectedItem, setSelectedItem } = useSelectedItem()

  const handleSelectItem = (
    item: ClothesBasicWithFileResponse & { imageUrl: string | null },
  ) => {
    if (selectedItem && selectedItem.clothesId === item.clothesId) {
      setSelectedItem(null)
    } else {
      setSelectedItem(item)
    }
  }

  const renderClothesImage = (
    item: ClothesBasicWithFileResponse & { imageUrl: string | null },
  ) => {
    const isImageProcessing = !item.imageFile && (item.processing ?? 0) > 0

    if (isImageProcessing) {
      return (
        <div className="mr-4 flex h-[75px] w-[75px] shrink-0 items-center justify-center bg-gray-100 px-2 text-center text-xs font-medium text-gray-500">
          이미지 처리중
        </div>
      )
    }

    return (
      <Image
        src={item.imageUrl ?? '/images/mockup/tops11.png'}
        alt={item.name ?? 'No name'}
        width={75}
        height={75}
        className="mr-4 aspect-square object-contain"
        priority
      />
    )
  }

  return (
    <div className="flex flex-col justify-start items-center">
      {clothingList.map((item) =>
        isSelectable ? (
          <div
            role="presentation"
            key={item.clothesId}
            className={`px-5 w-full hover:bg-primary-100 active:bg-primary-100 cursor-pointer transition duration-150 ${
              selectedItem && selectedItem.clothesId === item.clothesId
                ? 'bg-primary-100'
                : ''
            }`}
            onClick={() => handleSelectItem(item)}
          >
            <div className="flex items-center mb-4 mt-4">
              {renderClothesImage(item)}
              <div>
                <div className="text-sm text-gray-500">
                  {item.createdDate
                    ? formatDate(item.createdDate)
                    : 'No date available'}
                </div>
                <div className="text-lg font-semibold">
                  {item.name ?? 'Unnamed Item'}
                </div>
              </div>
            </div>
            <hr />
          </div>
        ) : (
          <Link
            key={item.clothesId}
            href={`/closet/modify/${item.clothesId}`}
            passHref
            className="px-5 w-full hover:bg-primary-100 active:bg-primary-100  transition duration-150"
          >
            <div className="flex items-center mb-4 mt-4 cursor-pointer">
              {renderClothesImage(item)}
              <div>
                <div className="text-sm text-gray-500">
                  {item.createdDate
                    ? formatDate(item.createdDate)
                    : 'No date available'}
                </div>
                <div className="text-lg font-semibold">
                  {item.name ?? 'Unnamed Item'}
                </div>
              </div>
            </div>
            <hr />
          </Link>
        ),
      )}
    </div>
  )
}
