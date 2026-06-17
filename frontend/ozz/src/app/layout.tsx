import DemoAuthHeader from '@/components/DemoAuthHeader'
import Navbar from '@/components/Navbar'
import { SelectedColorProvider } from '@/contexts/SelectedColorContext'
import { SelectedItemProvider } from '@/contexts/SelectedItemContext'
import { CategorySidebarProvider } from '@/contexts/CategorySidebarContext'
import '@/styles/global.css'
import '@sjw-project/demo-header/style.css'
import localFont from 'next/font/local'
import type { Viewport } from 'next'
import Providers from './providers'

const pretendard = localFont({
  src: '../../public/fonts/PretendardVariable.woff2',
  display: 'swap',
  weight: '45 920',
  variable: '--font-pretendard',
})

const APP_NAME = '옷짱 | OZZ'
const APP_DEFAULT_TITLE = '옷짱 | OZZ'
const APP_TITLE_TEMPLATE = '%s '
const APP_DESCRIPTION = '내 손안의 작은 옷장'

export const metadata = {
  applicationName: APP_NAME,
  title: {
    default: APP_DEFAULT_TITLE,
    template: APP_TITLE_TEMPLATE,
  },
  description: APP_DESCRIPTION,
  formatDetection: {
    telephone: false,
  },
  openGraph: {
    type: 'website',
    siteName: APP_NAME,
    title: {
      default: APP_DEFAULT_TITLE,
      template: APP_TITLE_TEMPLATE,
    },
    description: APP_DESCRIPTION,
  },
  twitter: {
    card: 'summary',
    title: {
      default: APP_DEFAULT_TITLE,
      template: APP_TITLE_TEMPLATE,
    },
    description: APP_DESCRIPTION,
  },
  icons: {
    icon: '/favicon-144x144.png',
  },
}

export const viewport: Viewport = {
  width: 'device-width',
  initialScale: 1,
  maximumScale: 1,
  minimumScale: 1,
  userScalable: false,
  themeColor: '#3e3e3e',
}

export default function RootLayout({
  children,
}: {
  children: React.ReactNode
}) {
  return (
    <html lang="ko" className={`${pretendard.variable}`}>
      <body className={`${pretendard.className}`}>
        <DemoAuthHeader />
        <Providers>
          <CategorySidebarProvider>
            <SelectedItemProvider>
              <SelectedColorProvider>
                <div id="wrapper">
                  <div className="pt-20 pb-16">{children}</div>
                  <Navbar />
                </div>
              </SelectedColorProvider>
            </SelectedItemProvider>
          </CategorySidebarProvider>
        </Providers>
      </body>
    </html>
  )
}
