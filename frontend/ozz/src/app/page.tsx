import SignIn from '@/containers/signin/login'
import { cookies } from 'next/headers'
import { redirect } from 'next/navigation'

export default function Home() {
  const cookieStore = cookies()
  const accessToken = cookieStore.get('access')

  if (accessToken) {
    redirect('/closet')
  }

  return <SignIn />
}
