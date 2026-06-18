import { NextResponse } from 'next/server'

const getAuthUrl = () => {
  return process.env.OZZ_AUTH_URL || 'http://localhost:8080'
}

// eslint-disable-next-line import/prefer-default-export
export async function POST() {
  const authResponse = await fetch(`${getAuthUrl()}/login/guest`, {
    redirect: 'manual',
  })

  const access = authResponse.headers.get('access')
  const refresh = authResponse.headers.get('refresh')

  if (!access || !refresh) {
    return NextResponse.json(
      { message: 'Demo login failed' },
      { status: authResponse.status || 500 },
    )
  }

  const response = NextResponse.json({ ok: true })

  response.cookies.set('access', access, { path: '/' })
  response.cookies.set('refresh', refresh, { path: '/' })

  return response
}
