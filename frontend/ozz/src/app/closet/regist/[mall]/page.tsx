import MallLoginForm from '@/containers/closet-page/MallLoginForm'
import { notFound } from 'next/navigation'

export default function MallLogin({ params }: { params: { mall: string } }) {
  if (params.mall !== 'musinsa') {
    notFound()
  }

  return <MallLoginForm mall={params.mall} />
}
