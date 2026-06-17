'use client'

import {
  DemoHeaderView,
  createDemoAuthClient,
  useDemoAuth,
  type DemoMe,
  type DemoProject,
} from '@sjw-project/demo-header'
import { useCallback, useMemo } from 'react'
import { removeTokens } from '@/services/authApi'

const DEMO_AUTH_URL =
  process.env.NEXT_PUBLIC_DEMO_AUTH_URL || 'https://auth.sjw-project.site'

const hasOzzAccessToken = () => {
  return document.cookie
    .split(';')
    .some((cookie) => cookie.trim().startsWith('access='))
}

const getOzzGuestLoginUrl = () => {
  return window.location.hostname === 'localhost'
    ? 'http://localhost:8000/login/guest'
    : '/login/guest'
}

const SYNCING_KEY = 'ozz-demo-login-syncing'

export default function DemoAuthHeader() {
  const authClient = useMemo(() => createDemoAuthClient(DEMO_AUTH_URL), [])

  const projects = useMemo<DemoProject[]>(() => {
    const currentOrigin =
      typeof window === 'undefined'
        ? 'http://localhost:3000'
        : window.location.origin

    return [
      { name: 'Demo Home', url: DEMO_AUTH_URL },
      { name: 'OZZ', url: currentOrigin },
    ]
  }, [])

  const syncOzzAuth = useCallback((nextMe: DemoMe) => {
    const hasAccess = hasOzzAccessToken()
    const isSyncing = sessionStorage.getItem(SYNCING_KEY) === 'true'

    if (nextMe.loggedIn) {
      if (!hasAccess && !isSyncing) {
        sessionStorage.setItem(SYNCING_KEY, 'true')
        window.location.href = getOzzGuestLoginUrl()
      } else {
        sessionStorage.removeItem(SYNCING_KEY)
      }

      return
    }

    sessionStorage.removeItem(SYNCING_KEY)

    if (hasAccess) {
      removeTokens()

      if (window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
  }, [])

  const { me, loading, busy, error, handleDemoLogin, handleLogout } =
    useDemoAuth({
      authClient,
      onMeChanged: syncOzzAuth,
    })

  return (
    <DemoHeaderView
      projects={projects}
      me={me}
      loading={loading}
      busy={busy}
      error={error}
      onDemoLogin={handleDemoLogin}
      onLogout={handleLogout}
    />
  )
}
