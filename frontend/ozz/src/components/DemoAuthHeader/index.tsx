'use client'

import {
  DemoHeaderView,
  createDemoAuthClient,
  useDemoAuth,
  type DemoAuthClient,
  type DemoMe,
  type DemoProject,
} from '@sjw-project/demo-header'
import { useCallback, useEffect, useMemo, useState } from 'react'
import { removeTokens } from '@/services/authApi'

const DEMO_AUTH_URL =
  process.env.NEXT_PUBLIC_DEMO_AUTH_URL || 'https://auth.sjw-project.site'

const MOCK_COOKIE_NAME = 'sjw_demo_mock'
const SYNCING_KEY = 'ozz-demo-login-syncing'

const hasOzzAccessToken = () => {
  return document.cookie
    .split(';')
    .some((cookie) => cookie.trim().startsWith('access='))
}

const hasMockCookie = () => {
  return document.cookie
    .split(';')
    .some((cookie) => cookie.trim().startsWith(`${MOCK_COOKIE_NAME}=`))
}

const createLocalMockAuthClient = (): DemoAuthClient => {
  return {
    async fetchMe() {
      return hasMockCookie()
        ? {
            loggedIn: true,
            user: {
              id: 'local-demo',
              nickname: '로컬 데모',
              role: 'DEMO',
            },
          }
        : {
            loggedIn: false,
            user: null,
          }
    },
    async login() {
      document.cookie = `${MOCK_COOKIE_NAME}=local-demo; Max-Age=86400; path=/; SameSite=Lax`
    },
    async logout() {
      document.cookie = `${MOCK_COOKIE_NAME}=; Max-Age=0; path=/`
    },
  }
}

const isLocalhost = () => {
  return window.location.hostname === 'localhost'
}

const requestOzzDemoLogin = async () => {
  const response = await fetch('/api/demo-guest-login', {
    method: 'POST',
    credentials: 'include',
  })

  if (!response.ok) {
    throw new Error('Demo login failed')
  }
}

export default function DemoAuthHeader() {
  const [projects, setProjects] = useState<DemoProject[]>([])

  const authClient = useMemo(() => {
    if (typeof window !== 'undefined' && isLocalhost()) {
      return createLocalMockAuthClient()
    }

    return createDemoAuthClient(DEMO_AUTH_URL)
  }, [])

  useEffect(() => {
    let cancelled = false
    const projectClient = createDemoAuthClient(DEMO_AUTH_URL)

    projectClient
      .fetchProjects?.()
      .then((nextProjects) => {
        if (!cancelled) {
          setProjects(nextProjects)
        }
      })
      .catch(() => {
        if (!cancelled) {
          setProjects([])
        }
      })

    return () => {
      cancelled = true
    }
  }, [])

  const syncOzzAuth = useCallback(async (nextMe: DemoMe) => {
    const hasAccess = hasOzzAccessToken()
    const isSyncing = sessionStorage.getItem(SYNCING_KEY) === 'true'

    if (nextMe.loggedIn) {
      if (!hasAccess && !isSyncing) {
        sessionStorage.setItem(SYNCING_KEY, 'true')
        await requestOzzDemoLogin()
        window.location.href = '/closet'
      } else {
        sessionStorage.removeItem(SYNCING_KEY)
      }

      return
    }

    sessionStorage.removeItem(SYNCING_KEY)

    if (hasAccess) {
      removeTokens()

      if (window.location.pathname !== '/') {
        window.location.href = '/'
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
