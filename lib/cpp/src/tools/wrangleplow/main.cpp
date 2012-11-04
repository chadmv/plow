#include "wrangleplow.h"
#include <QApplication>

int main(int argc, char *argv[])
{
  QApplication app(argc, argv);

  WranglePlow::MainWindow wrangleplow;
  wrangleplow.show();
  wrangleplow.resize(1024, 768);
  wrangleplow.move(100, 100);

  return app.exec();
}